/*
 * Copyright (c) 2021 Villu Ruusmann
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
package sklearn;

import java.util.ArrayList;
import java.util.List;

import org.jpmml.converter.FieldNameUtil;

public interface HasMultiApplyField extends HasApplyField {

	int getNumberOfApplyFields();

	default
	List<String> getApplyFields(){
		List<String> result = new ArrayList<>();

		for(int i = 0, max = getNumberOfApplyFields(); i < max; i++){
			String name = getApplyField(i + 1);

			result.add(name);
		}

		return result;
	}

	default
	String getApplyField(Object segmentId){
		return FieldNameUtil.create(getApplyField(), segmentId);
	}
}