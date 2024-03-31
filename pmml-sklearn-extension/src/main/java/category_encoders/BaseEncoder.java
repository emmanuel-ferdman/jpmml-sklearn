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

import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;
import sklearn.Transformer;

abstract
public class BaseEncoder extends Transformer {

	public BaseEncoder(String module, String name){
		super(module, name);
	}

	@Override
	public OpType getOpType(){
		return OpType.CATEGORICAL;
	}

	@Override
	public DataType getDataType(){
		return DataType.STRING;
	}

	public List<?> getCols(){
		return getList("cols");
	}

	public List<String> getInvariantCols(){

		// CategoryEncoders 2.3
		if(containsKey("drop_cols")){
			return getList("drop_cols", String.class);
		}

		// CategoryEncoders 2.5+
		return getList("invariant_cols", String.class);
	}

	public Boolean getDropInvariant(){
		return getBoolean("drop_invariant");
	}

	public List<String> getFeatureNamesOut(){

		// CategoryEncoders 2.5.1post0
		if(containsKey("feature_names")){
			return getList("feature_names", String.class);
		}

		// CategoryEncoders 2.6+
		return getList("feature_names_out_", String.class);
	}

	public String getHandleMissing(){
		return getString("handle_missing");
	}

	public String getHandleUnknown(){
		return getString("handle_unknown");
	}

	public static final Object CATEGORY_NAN = Double.NaN;
}