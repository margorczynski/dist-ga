package org.margorczynski

package object distevolve {
  type Chromosome = String

  case class ChromosomeWithFitness(chromosome: Chromosome, fitness: Double)
}
