/*
 * Copyright (c) 2020 Villu Ruusmann
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
package category_encoders;

import java.util.List;

import org.jpmml.converter.Feature;
import org.jpmml.sklearn.SkLearnEncoder;

public class BinaryEncoder extends BaseNEncoder {

	public BinaryEncoder(String module, String name){
		super(module, name);
	}

	@Override
	public List<Feature> encode(List<Feature> features, SkLearnEncoder encoder){
		BaseNEncoder baseNEncoder = getBaseNEncoder();

		if(baseNEncoder != this){
			return baseNEncoder.encode(features, encoder);
		}

		return super.encode(features, encoder);
	}

	public BaseNEncoder getBaseNEncoder(){

		// CategoryEncoders 2.3
		if(containsKey("base_n_encoder")){
			return get("base_n_encoder", BaseNEncoder.class);
		}

		// CategoryEncoders 2.5+
		return this;
	}
}