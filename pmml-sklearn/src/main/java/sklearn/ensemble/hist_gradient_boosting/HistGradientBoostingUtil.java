/*
 * Copyright (c) 2019 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn.ensemble.hist_gradient_boosting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dmg.pmml.DataType;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segmentation;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.converter.ContinuousLabel;
import org.jpmml.converter.Feature;
import org.jpmml.converter.Label;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.PredicateManager;
import org.jpmml.converter.Schema;
import org.jpmml.converter.mining.MiningModelUtil;
import org.jpmml.sklearn.SkLearnEncoder;
import sklearn.compose.ColumnTransformer;

public class HistGradientBoostingUtil {

	private HistGradientBoostingUtil(){
	}

	static
	public Schema preprocess(ColumnTransformer preprocessor, Schema schema){
		SkLearnEncoder encoder = (SkLearnEncoder)schema.getEncoder();

		Label label = schema.getLabel();
		List<Feature> features = new ArrayList<>(schema.getFeatures());

		features = preprocessor.encode(features, encoder);

		return new Schema(encoder, label, features);
	}

	static
	public MiningModel encodeHistGradientBoosting(List<List<TreePredictor>> predictors, BinMapper binMapper, List<? extends Number> baselinePredictions, int column, Schema schema){
		List<TreePredictor> treePredictors = predictors.stream()
			.map(predictor -> predictor.get(column))
			.collect(Collectors.toList());

		Number baselinePrediction = baselinePredictions.get(column);

		return encodeHistGradientBoosting(treePredictors, binMapper, baselinePrediction, schema);
	}

	static
	public MiningModel encodeHistGradientBoosting(List<TreePredictor> treePredictors, BinMapper binMapper, Number baselinePrediction, Schema schema){
		ContinuousLabel continuousLabel = (ContinuousLabel)schema.getLabel();

		PredicateManager predicateManager = new PredicateManager();

		Schema segmentSchema = schema.toAnonymousRegressorSchema(DataType.DOUBLE);

		List<TreeModel> treeModels = new ArrayList<>();

		for(TreePredictor treePredictor : treePredictors){
			TreeModel treeModel = TreePredictorUtil.encodeTreeModel(treePredictor, binMapper, predicateManager, segmentSchema);

			treeModels.add(treeModel);
		}

		MiningModel miningModel = new MiningModel(MiningFunction.REGRESSION, ModelUtil.createMiningSchema(continuousLabel))
			.setSegmentation(MiningModelUtil.createSegmentation(Segmentation.MultipleModelMethod.SUM, Segmentation.MissingPredictionTreatment.RETURN_MISSING, treeModels))
			.setTargets(ModelUtil.createRescaleTargets(null, baselinePrediction, continuousLabel));

		return miningModel;
	}
}