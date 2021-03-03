package org.margorczynski.distevolve

import org.scalatest._
import flatspec._
import org.scalatest._
import matchers.should._
import EvolutionLogic._

class EvolutionLogicSpec extends AnyFlatSpec with Matchers {

  //The random number should be provided without side-effects for this to be effectively testable

  "A mutation" should "not change a chromosome if the rate is 0%" in {
    val chromosome = "0011001010"

    mutate(chromosome, 0.0D) shouldBe chromosome
  }

  "A mutation" should "invert the chromosome if the rate is 100%" in {
    val chromosome = "0011001010"
    val invertedChromosome = "1100110101"

    mutate(chromosome, 1.0D) shouldBe invertedChromosome
  }

  "A crossover" should "produce the same amount of chromosomes as the number of crossed over chromosomes" in {
    crossover(testChromosomes, 0.0D).size shouldBe testChromosomes.size
    crossover(testChromosomes, 0.25D).size shouldBe testChromosomes.size
    crossover(testChromosomes, 0.5D).size shouldBe testChromosomes.size
    crossover(testChromosomes, 0.75D).size shouldBe testChromosomes.size
    crossover(testChromosomes, 1.0D).size shouldBe testChromosomes.size
  }

  "A crossover" should "pass the exact same chromosomes if the crossover rate is 0%" in {
    crossover(testChromosomes, 0.0D) should contain theSameElementsAs testChromosomes
  }

  //This is pretty shaky as theoretically there is an non-zero chance in many cases they might be identical
  "A crossover" should "not pass the exact same chromosomes if the crossover rate is 100%" in {
    crossover(testChromosomes, 1.0D) should not contain theSameElementsAs(testChromosomes)
  }

  private val testChromosomes = Seq(
    "01000110",
    "00001000",
    "01100110",
    "00000000",
    "11111011"
  )
}