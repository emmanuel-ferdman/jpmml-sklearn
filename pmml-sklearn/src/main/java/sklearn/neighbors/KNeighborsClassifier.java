/*
 * Copyright (c) 2016 Villu Ruusmann
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
package sklearn.neighbors;

import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.nearest_neighbor.NearestNeighborModel;
import org.jpmml.converter.Schema;
import sklearn.SkLearnClassifier;

public class KNeighborsClassifier extends SkLearnClassifier implements HasMetric, HasNumberOfNeighbors, HasTrainingData, KNeighborsConstants {

	public KNeighborsClassifier(String module, String name){
		super(module, name);
	}

	@Override
	public int getNumberOfFeatures(){
		int[] shape = getFitXShape();

		return shape[1];
	}

	@Override
	public int getNumberOfOutputs(){
		return KNeighborsUtil.getNumberOfOutputs(this);
	}

	@Override
	public DataType getDataType(){
		return DataType.FLOAT;
	}

	@Override
	public boolean hasProbabilityDistribution(){
		return false;
	}

	@Override
	public NearestNeighborModel encodeModel(Schema schema){
		int[] shape = getFitXShape();
		String weights = getWeights();

		int numberOfInstances = shape[0];
		int numberOfFeatures = shape[1];

		boolean weighted = KNeighborsUtil.parseWeights(weights);

		NearestNeighborModel nearestNeighborModel = KNeighborsUtil.encodeNeighbors(this, MiningFunction.CLASSIFICATION, numberOfInstances, numberOfFeatures, schema)
			.setCategoricalScoringMethod(weighted ? NearestNeighborModel.CategoricalScoringMethod.WEIGHTED_MAJORITY_VOTE : NearestNeighborModel.CategoricalScoringMethod.MAJORITY_VOTE)
			.setThreshold(weighted ? 0d : null);

		return nearestNeighborModel;
	}

	@Override
	public String getMetric(){
		return getEnum("metric", this::getString, KNeighborsClassifier.ENUM_METRIC);
	}

	@Override
	public int getP(){
		return getInteger("p");
	}

	@Override
	public int getNumberOfNeighbors(){
		return getInteger("n_neighbors");
	}

	@Override
	public List<Number> getFitX(){
		return getNumberArray("_fit_X");
	}

	@Override
	public int[] getFitXShape(){
		return getArrayShape("_fit_X", 2);
	}

	@Override
	public List<Object> getId(){

		if(!hasattr("_id")){
			return null;
		}

		return getObjectArray("_id");
	}

	@Override
	public List<Number> getY(){
		return getNumberArray("_y");
	}

	@Override
	public int[] getYShape(){
		return getArrayShape("_y");
	}

	public String getWeights(){
		return getEnum("weights", this::getString, KNeighborsClassifier.ENUM_WEIGHTS);
	}
}