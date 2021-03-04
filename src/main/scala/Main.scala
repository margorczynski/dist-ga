package org.margorczynski.distga

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("dist-ga")

  //TODO: Remove the hardcoding - either to application.conf or app args

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

  val bootstrapServers = "localhost:9092"

  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("dist-ga-server")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  val inputTopic = "dist-ga-chromosome-with-fitness"
  val outputTopic = "dist-ga-chromosome"

  val initialPopulationSource =
    Source(EvolutionLogic.generateInitialPopulation(50, 31))
  val kafkaChromosomeWithFitnessSource =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(inputTopic))

  val kafkaChromosomeTopicSink =
    Producer.plainSink(producerSettings)

  val evolvedChromosomeSource =
    kafkaChromosomeWithFitnessSource
      .grouped(8)
      .mapConcat { chromosomeWithFitnessMessages =>
        val chromosomesWithFitness = chromosomeWithFitnessMessages.map(msg => ChromosomeWithFitness(msg.key, msg.value.toDouble))

        val selectedChromosomes = EvolutionLogic.select(chromosomesWithFitness, Tournament(8))
        val crossoverResult = EvolutionLogic.crossover(selectedChromosomes, 0.90D)
        val resultAfterMutations = crossoverResult.map(EvolutionLogic.mutate(_, 0.01D))

        resultAfterMutations
      }

  initialPopulationSource
    .concat(evolvedChromosomeSource)
    .map(c => new ProducerRecord[String, String](outputTopic, c))
    .runWith(kafkaChromosomeTopicSink)
}