/*
 * Copyright (c) 2017 Villu Ruusmann
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
package sklearn.impute;

import java.util.Collections;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.MissingValueTreatmentMethod;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMMLFunctions;
import org.dmg.pmml.Value;
import org.jpmml.converter.BooleanFeature;
import org.jpmml.converter.ExpressionUtil;
import org.jpmml.converter.Feature;
import org.jpmml.converter.FeatureUtil;
import org.jpmml.converter.FieldUtil;
import org.jpmml.converter.MissingValueDecorator;
import org.jpmml.model.UnsupportedElementException;
import org.jpmml.sklearn.SkLearnEncoder;
import sklearn.Transformer;

public class ImputerUtil {

	private ImputerUtil(){
	}

	static
	public Feature encodeFeature(Transformer transformer, Feature feature, Boolean addIndicator, Object missingValue, Object replacementValue, MissingValueTreatmentMethod missingValueTreatmentMethod, SkLearnEncoder encoder){
		Field<?> field = feature.getField();

		if(field instanceof DataField && !addIndicator){
			DataField dataField = (DataField)field;

			encoder.addDecorator(dataField, new MissingValueDecorator(missingValueTreatmentMethod, replacementValue));

			if(missingValue != null){
				FieldUtil.addValues(dataField, Value.Property.MISSING, Collections.singletonList(missingValue));
			}

			return feature;
		} // End if

		if((field instanceof DataField) || (field instanceof DerivedField)){
			Expression expression = feature.ref();

			if(missingValue != null){
				expression = ExpressionUtil.createApply(PMMLFunctions.EQUAL, expression, ExpressionUtil.createConstant(feature.getDataType(), missingValue));
			} else

			{
				expression = ExpressionUtil.createApply(PMMLFunctions.ISMISSING, expression);
			}

			expression = ExpressionUtil.createApply(PMMLFunctions.IF,
				expression,
				ExpressionUtil.createConstant(feature.getDataType(), replacementValue),
				feature.ref()
			);

			DerivedField derivedField = encoder.createDerivedField(transformer.createFieldName("imputer", feature), field.requireOpType(), field.requireDataType(), expression);

			return FeatureUtil.createFeature(derivedField, encoder);
		} else

		{
			throw new UnsupportedElementException(field);
		}
	}

	static
	public Feature encodeIndicatorFeature(Transformer transformer, Feature feature, Object missingValue, SkLearnEncoder encoder){
		Expression expression = feature.ref();

		if(missingValue != null){
			expression = ExpressionUtil.createApply(PMMLFunctions.EQUAL, expression, ExpressionUtil.createConstant(feature.getDataType(), missingValue));
		} else

		{
			expression = ExpressionUtil.createApply(PMMLFunctions.ISMISSING, expression);
		}

		DerivedField derivedField = encoder.createDerivedField(transformer.createFieldName("missingIndicator", feature), OpType.CATEGORICAL, DataType.BOOLEAN, expression);

		return new BooleanFeature(encoder, derivedField);
	}
}
