/*
 * Copyright (c) 2018 Villu Ruusmann
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
package sklearn.svm;

import java.util.Arrays;

import org.dmg.pmml.Model;
import org.jpmml.converter.Schema;
import sklearn.linear_model.LinearClassifier;

public class LinearSVC extends LinearClassifier {

	public LinearSVC(String module, String name){
		super(module, name);
	}

	@Override
	public boolean hasProbabilityDistribution(){
		return false;
	}

	@Override
	public Model encodeModel(Schema schema){
		@SuppressWarnings("unused")
		String multiClass = getMultiClass();

		return super.encodeModel(schema);
	}

	public String getMultiClass(){
		return getEnum("multi_class", this::getString, Arrays.asList(LinearSVC.MULTICLASS_OVR));
	}

	private static final String MULTICLASS_OVR = "ovr";
}