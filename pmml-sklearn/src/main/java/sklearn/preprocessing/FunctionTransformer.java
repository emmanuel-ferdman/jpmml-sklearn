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
package sklearn.preprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.OpType;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.python.FunctionUtil;
import org.jpmml.python.Identifiable;
import org.jpmml.sklearn.SkLearnEncoder;
import sklearn.SkLearnTransformer;

public class FunctionTransformer extends SkLearnTransformer {

	public FunctionTransformer(String module, String name){
		super(module, name);
	}

	@Override
	public List<Feature> encodeFeatures(List<Feature> features, SkLearnEncoder encoder){
		Identifiable func = getFunc();

		if(func == null){
			return features;
		}

		List<Feature> result = new ArrayList<>();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);

			ContinuousFeature continuousFeature = feature.toContinuousFeature();

			Expression expression = FunctionUtil.encodeFunction(func, Collections.singletonList(continuousFeature.ref()));

			DerivedField derivedField = encoder.createDerivedField(createFieldName(func.getName(), continuousFeature), OpType.CONTINUOUS, DataType.DOUBLE, expression);

			result.add(new ContinuousFeature(encoder, derivedField));
		}

		return result;
	}

	public Identifiable getFunc(){
		return getOptional("func", Identifiable.class);
	}

	public Identifiable getInverseFunc(){
		return getOptional("inverse_func", Identifiable.class);
	}
}