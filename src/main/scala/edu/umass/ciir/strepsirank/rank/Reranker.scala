package edu.umass.ciir.strepsirank.rank

import ciir.umass.edu.learning.{RANKER_TYPE, RankerTrainer}
import ciir.umass.edu.metric.{MetricScorer, APScorer}
import scala.collection.JavaConversions._
import RankTools.MultiRankings

/**
 * Main re-rank interface
 *
 *
 * User: dietz
 * Date: 12/6/13
 * Time: 5:04 PM
 */
class Reranker(rankertype:RANKER_TYPE = RANKER_TYPE.COOR_ASCENT, metricScorer:MetricScorer = new APScorer()) {

  def trainPredict(train:MultiRankings, modelfilename:Option[String]=None, testData:Option[MultiRankings]=None):Option[MultiRankings] = {
    val rankListConv = new RankListConv(trackIgnoreFeature = false, ignoreNewFeatures = false)

    val trainingList = rankListConv.multiDataToRankList( train)
    val rt = new RankerTrainer()
    val featureIndices = rankListConv.fc.featureIndices

    val ranker = rt.train(rankertype, trainingList, featureIndices, metricScorer)

    modelfilename match {
      case Some(filename) =>
        ranker.save(modelfilename + ".ranklib")
        rankListConv.fc.save(modelfilename + ".featureconv")
      case _ => {}
    }

    testData.map { test => {
        val toPredictList = rankListConv.multiDataToRankList( test)
        val rankLists = ranker.rank(toPredictList)
        val predictedRankings = rankListConv.rankListsToMultiData(
          rankLists, test, ranker.eval)
        predictedRankings
      }
    }
  }
  
  def loadPredict(modelfilename:String,testData:MultiRankings ):MultiRankings = {
    val rt2 = new RankerTrainer()
    val rankListConv = new RankListConv(trackIgnoreFeature = false, ignoreNewFeatures = true)
    rankListConv.fc.load(modelfilename + ".featureconv")
    val ranker = rt2.createEmptyRanker(rankertype, rankListConv.fc.featureIndices, metricScorer)
    ranker.load(modelfilename + ".ranklib")

    val toPredictList = rankListConv.multiDataToRankList( testData)
    val rankLists = ranker.rank(toPredictList)
    val predictedRankings = rankListConv.rankListsToMultiData( rankLists, testData, ranker.eval)
    predictedRankings
  }




}


