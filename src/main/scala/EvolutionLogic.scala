package org.margorczynski.distga

import com.typesafe.scalalogging.LazyLogging

import scala.math.round
import scala.util.Random

object EvolutionLogic extends LazyLogging {

  private val r = Random

  def generateInitialPopulation(size: Int, chromosomeSize: Int): Seq[Chromosome] = {
    logger.debug(s"Generating initial population of size $size and chromosome length $chromosomeSize")
    Seq.fill(size) {
      Seq.fill(chromosomeSize) {
        if (r.nextBoolean()) '1' else '0'
      }.mkString
    }
  }

  def select(chromosomesWithFitness: Seq[ChromosomeWithFitness], selectionStrategy: SelectionStrategy, elitismFactor: Double = 0.0D): Seq[Chromosome] = {
    logger.debug(s"Selection for population of ${chromosomesWithFitness.size} chromosomes")
    val eliteSplitIndex =
      round((chromosomesWithFitness.size - 1) * elitismFactor).toInt

    val (elitesWithFitness, nonElitesWithFitness) =
      chromosomesWithFitness.sortBy(c => -c.fitness).splitAt(eliteSplitIndex)

    val nonEliteSelected = selectionStrategy match {
      case Roulette => {

        val chromosomesWithCumulativeFitness = nonElitesWithFitness.scanLeft(ChromosomeWithFitness("", 0.0D)) {
          case (prev, curr) => curr.copy(fitness = prev.fitness + curr.fitness)
        }

        (0 to chromosomesWithCumulativeFitness.size / 2).toList.flatMap { _ =>
          val randomChoice = r.between(0.0D, chromosomesWithCumulativeFitness.map(_.fitness).max)

          chromosomesWithCumulativeFitness.find(_.fitness >= randomChoice).map(_.chromosome)
        }
      }
      case Tournament(participantCount) =>
        r.shuffle(nonElitesWithFitness).grouped(participantCount).map(_.maxBy(_.fitness).chromosome).toList
    }

    logger.debug(s"Elite count after selection: ${elitesWithFitness.size}")
    logger.debug(s"Non-elite count after selection: ${nonEliteSelected.size}")

    r.shuffle(nonEliteSelected ++ elitesWithFitness.map(_.chromosome))
  }

  def crossover(chromosomes: Seq[Chromosome], crossoverRate: Double): Seq[Chromosome] = {
    logger.debug(s"Crossover for population of ${chromosomes.size} with rate $crossoverRate")
    chromosomes.grouped(2).toList.flatMap {
      case firstChromosome :: secondChromosome :: Nil =>
        //TODO: Parametrize the crossover strategy?
        if (r.nextDouble < crossoverRate) {
          logger.debug(s"Crossing two chromosomes: $firstChromosome and $secondChromosome")
          //At least crossover one bit so it doesn't do "total swaps" of chromosomes
          val crossoverPoint = r.between(1, firstChromosome.size - 2)
          val (leftFirst, rightFirst) = firstChromosome.splitAt(crossoverPoint)
          val (leftSecond, rightSecond) = secondChromosome.splitAt(crossoverPoint)

          val result = Seq(
            leftFirst ++ rightSecond,
            leftSecond ++ rightFirst
          )

          logger.debug(s"Crossover result: $result")

          result
        } else {
          logger.debug("No crossover, leaving as-is")
          Seq(firstChromosome, secondChromosome)
        }
      case oneOrNone =>
        logger.debug(s"Only single chromosome or none given for crossover: $oneOrNone")
        oneOrNone
    }
  }

  def mutate(chromosome: Chromosome, mutationRate: Double): Chromosome =
    chromosome.map { bit =>
      if (r.nextDouble < mutationRate)
        if (bit == '0') '1' else '0'
      else
        bit
    }
}