package org.margorczynski

package object distga {
  type Chromosome = String

  case class ChromosomeWithFitness(chromosome: Chromosome, fitness: Double)

}
