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

import java.util.Collections;
import java.util.List;

import org.dmg.pmml.mining.MiningModel;
import org.jpmml.converter.Schema;
import sklearn.SkLearnRegressor;
import sklearn.compose.ColumnTransformer;

public class HistGradientBoostingRegressor extends SkLearnRegressor {

	public HistGradientBoostingRegressor(String module, String name){
		super(module, name);
	}

	@Override
	public MiningModel encodeModel(Schema schema){
		Number baselinePrediction = getBaselinePrediction();
		BinMapper binMapper = getBinMapper();
		List<List<TreePredictor>> predictors = getPredictors();
		ColumnTransformer preprocessor = getPreprocessor();

		if(preprocessor != null){
			schema = HistGradientBoostingUtil.preprocess(preprocessor, schema);
		}

		return HistGradientBoostingUtil.encodeHistGradientBoosting(predictors, binMapper, Collections.singletonList(baselinePrediction), 0, schema);
	}

	public Number getBaselinePrediction(){

		// SkLearn 1.0.2
		try {
			return getNumber("_baseline_prediction");
		// SkLearn 1.1.0+
		} catch(IllegalArgumentException iae){
			List<Number> baselinePredictions = getNumberArray("_baseline_prediction");

			if(baselinePredictions.size() != 1){
				throw new IllegalArgumentException();
			}

			return baselinePredictions.get(0);
		}
	}

	public BinMapper getBinMapper(){
		return getOptional("_bin_mapper", BinMapper.class);
	}

	public List<List<TreePredictor>> getPredictors(){
		return (List)getList("_predictors", List.class);
	}

	public ColumnTransformer getPreprocessor(){
		return getOptional("_preprocessor", ColumnTransformer.class);
	}
}