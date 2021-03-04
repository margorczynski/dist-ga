package org.margorczynski.distevolve

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, RunnableGraph, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{DoubleDeserializer, DoubleSerializer, StringDeserializer, StringSerializer}

object ClientMain extends App {

  implicit val system: ActorSystem = ActorSystem("dist-evolve-client")

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

  val bootstrapServers = "localhost:9092"

  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("dist-evolve-client")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new DoubleSerializer)
      .withBootstrapServers(bootstrapServers)

  val inputTopic = "dist-evolve-chromosome"
  val outputTopic = "dist-evolve-chromosome-with-fitness"

  val kafkaChromosomeSource =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(inputTopic))

  val kafkaChromosomeWithFitnessSink =
    Producer.plainSink(producerSettings)

  kafkaChromosomeSource
    .map { message =>
      val chromosome = message.value()

      new ProducerRecord[String, java.lang.Double](outputTopic, chromosome, computeFitness(chromosome))
    }
    .runWith(kafkaChromosomeWithFitnessSink)


  //5x-15=0
  private def computeFitness(chromosome: Chromosome): Double = {

    val value = Integer.parseInt(chromosome, 2)

    val result = (5 * value) - 15

    println(result)

    if(result != 0.0D) 1.0D/scala.math.abs(result) else Double.MaxValue
  }
}