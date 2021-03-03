package org.margorczynski.distevolve

import scala.math.round
import scala.util.Random

object EvolutionLogic {

  private val r = Random

  def generateInitialPopulation(size: Int, chromosomeSize: Int): Seq[Chromosome] = {
    Seq.fill(size) {
      Seq.fill(chromosomeSize) {
        if(r.nextBoolean()) '1' else '0'
      }.mkString
    }
  }

  def select(chromosomesWithFitness: Seq[ChromosomeWithFitness], selectionStrategy: SelectionStrategy, elitismFactor: Double = 0.0D): Seq[Chromosome] = {

    val eliteSplitIndex =
      round((chromosomesWithFitness.size - 1) * elitismFactor).toInt

    val (nonEliteWithFitness, eliteWithFitness) =
      chromosomesWithFitness.sortBy(_.fitness).splitAt(eliteSplitIndex)

    val nonEliteSelected = selectionStrategy match {
      case Roulette => {

        val chromosomesWithCumulativeFitness = nonEliteWithFitness.scanLeft(ChromosomeWithFitness("", 0.0D)) {
          case (prev, curr) => curr.copy(fitness = prev.fitness + curr.fitness)
        }

        (0 to chromosomesWithCumulativeFitness.size / 2).toList.flatMap { _ =>
          val randomChoice = r.between(0.0D, chromosomesWithCumulativeFitness.map(_.fitness).max)

          chromosomesWithCumulativeFitness.find(_.fitness >= randomChoice).map(_.chromosome)
        }
      }
      case Tournament(participantCount) =>
        r.shuffle(nonEliteWithFitness).grouped(participantCount).map(_.maxBy(_.fitness).chromosome).toList
    }

    r.shuffle(nonEliteSelected ++ eliteWithFitness.map(_.chromosome))
  }

  def crossover(chromosomes: Seq[Chromosome], crossoverRate: Double): Seq[Chromosome] =
    chromosomes.grouped(2).toList.flatMap {
      case firstChromosome :: secondChromosome :: Nil =>
        //TODO: Parametrize the crossover strategy?
        if(r.nextDouble < crossoverRate) {
          //At least crossover one bit so it doesn't do "total swaps" of chromosomes
          val crossoverPoint = r.between(1, firstChromosome.size - 2)
          val (leftFirst, rightFirst) = firstChromosome.splitAt(crossoverPoint)
          val (leftSecond, rightSecond) = secondChromosome.splitAt(crossoverPoint)

          Seq(
            leftFirst ++ rightSecond,
            leftSecond ++ rightFirst
          )
        } else {
          Seq(firstChromosome, secondChromosome)
        }
      case oneOrNone =>
        oneOrNone
    }

  def mutate(chromosome: Chromosome, mutationRate: Double): Chromosome =
    chromosome.map { bit =>
      if(r.nextDouble < mutationRate)
        if(bit == '0') '1' else '0'
      else
        bit
    }
}