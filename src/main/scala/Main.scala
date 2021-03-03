package org.margorczynski.distevolve

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, RunnableGraph, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{DoubleDeserializer, StringDeserializer, StringSerializer}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("dist-evolve")

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

  val bootstrapServers = "localhost:9092"

  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new DoubleDeserializer)
      .withBootstrapServers(bootstrapServers)
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  val inputTopic = "dist-evolve-chromosome-with-fitness"
  val outputTopic = "dist-evolve-chromosome"

  val initialPopulationSource =
    Source(EvolutionLogic.generateInitialPopulation(10, 5))
  val kafkaChromosomeWithFitnessSource =
    Consumer.committableSource(consumerSettings, Subscriptions.topics(inputTopic))

  val kafkaChromosomeTopicSink =
    Producer.plainSink(producerSettings)

  val evolvedChromosomeSource =
    kafkaChromosomeWithFitnessSource
      .grouped(6)
      .mapConcat { chromosomeWithFitnessMessages =>
        val chromosomesWithFitness = chromosomeWithFitnessMessages.map(msg => ChromosomeWithFitness(msg.record.key, msg.record.value))

        val selectedChromosomes = EvolutionLogic.select(chromosomesWithFitness, Tournament(2))
        val crossoverResult = EvolutionLogic.crossover(selectedChromosomes, 0.90D)
        val resultAfterMutations = crossoverResult.map(EvolutionLogic.mutate(_, 0.01D))

        resultAfterMutations
      }

  initialPopulationSource
    .concat(evolvedChromosomeSource)
    .map(c => new ProducerRecord[String, String](outputTopic, c))
    .runWith(kafkaChromosomeTopicSink)
}