package org.margorczynski.distga

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object ClientMain extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("dist-ga-client")

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

  val bootstrapServers = "localhost:9092"

  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("dist-ga-client")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  val inputTopic = "dist-ga-chromosome"
  val outputTopic = "dist-ga-chromosome-with-fitness"

  val kafkaChromosomeSource =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(inputTopic))

  val kafkaChromosomeWithFitnessSink =
    Producer.plainSink(producerSettings)

  kafkaChromosomeSource
    .map { message =>
      val chromosome = message.value()

      new ProducerRecord[String, String](outputTopic, chromosome, computeFitness(chromosome).toString)
    }
    .runWith(kafkaChromosomeWithFitnessSink)


  //Max f(x) = x
  private def computeFitness(chromosome: Chromosome): Double = {

    logger.debug(s"Compute fitness for $chromosome")

    val value = Integer.parseInt(chromosome, 2)

    val result = value
    val fitness = value

    fitness
  }
}