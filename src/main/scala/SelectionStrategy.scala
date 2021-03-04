package org.margorczynski.distga

sealed trait SelectionStrategy

case object Roulette extends SelectionStrategy

case class Tournament(participantCount: Int) extends SelectionStrategy