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
package category_encoders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import numpy.core.ScalarUtil;
import org.dmg.pmml.Field;
import org.dmg.pmml.InvalidValueTreatmentMethod;
import org.jpmml.converter.Feature;
import org.jpmml.converter.InvalidValueDecorator;
import org.jpmml.converter.ValueUtil;
import org.jpmml.python.ClassDictUtil;
import org.jpmml.sklearn.SkLearnEncoder;
import pandas.core.Series;
import pandas.core.SeriesUtil;
import sklearn.preprocessing.EncoderUtil;

public class CountEncoder extends MapEncoder implements BaseEncoderConstants {

	public CountEncoder(String module, String name){
		super(module, name);
	}

	@Override
	public String functionName(){
		return "count";
	}

	@Override
	public List<Feature> encodeFeatures(List<Feature> features, SkLearnEncoder encoder){
		List<?> cols = getCols();
		@SuppressWarnings("unused")
		Boolean dropInvariant = getDropInvariant();
		String handleMissing = getHandleMissing();
		String handleUnknown = getHandleUnknown();
		Boolean normalize = getNormalize();
		Map<Object, Series> mapping = getMapping();
		Map<Object, Map<Object, String>> minGroupCategories = getMinGroupCategories();

		Object missingCategory = null;

		if((CountEncoder.HANDLEMISSING_COUNT).equals(handleMissing) || (CountEncoder.HANDLEMISSING_VALUE).equals(handleMissing)){
			missingCategory = BaseEncoder.CATEGORY_NAN;
		}

		Integer defaultValue = null;

		if((CountEncoder.HANDLEUNKNOWN_VALUE).equals(handleUnknown)){
			defaultValue = getDefaultValue();
		}

		ClassDictUtil.checkSize(features, cols);

		List<Feature> result = new ArrayList<>();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);
			Object col = cols.get(i);

			Series series = mapping.get(col);

			Map<Object, Number> categoryCounts = SeriesUtil.toMap(series, Functions.identity(), normalize ? ValueUtil::asDouble : ValueUtil::asInteger);

			Map<?, String> leftoverCategories = minGroupCategories.get(col);
			if(leftoverCategories != null){
				String leftoverCategory = Iterables.getOnlyElement(new HashSet<>(leftoverCategories.values()));

				Number leftoverCount = categoryCounts.remove(leftoverCategory);
				if(leftoverCount == null){
					throw new IllegalArgumentException();
				}

				Set<?> categories = leftoverCategories.keySet();
				for(Object category : categories){
					categoryCounts.put(category, leftoverCount);
				}
			}

			List<Object> categories = new ArrayList<>(categoryCounts.keySet());

			Field<?> field = encoder.toCategorical(feature.getName(), EncoderUtil.filterCategories(categories));

			switch(handleUnknown){
				case CountEncoder.HANDLEUNKNOWN_VALUE:
					{
						EncoderUtil.addDecorator(field, new InvalidValueDecorator(InvalidValueTreatmentMethod.AS_IS, null), encoder);
					}
					break;
				default:
					break;
			}

			Feature mapFeature = new MapFeature(encoder, feature, categoryCounts, missingCategory, defaultValue){

				@Override
				public String getDerivedName(){
					return createFieldName(functionName(), getName());
				}
			};

			result.add(mapFeature);
		}

		return result;
	}

	public Integer getDefaultValue(){
		Object handleUnknown = getOptionalObject("handle_unknown");

		if(handleUnknown instanceof String){
			return 0;
		}

		return getInteger("handle_unknown");
	}

	@Override
	public String getHandleMissing(){
		return getEnum("handle_missing", this::getString, Arrays.asList(CountEncoder.HANDLEMISSING_ERROR, CountEncoder.HANDLEMISSING_RETURN_NAN, CountEncoder.HANDLEMISSING_COUNT, CountEncoder.HANDLEMISSING_VALUE));
	}

	@Override
	public String getHandleUnknown(){
		Object handleUnknown = getOptionalObject("handle_unknown");

		if(handleUnknown instanceof Integer){
			return CountEncoder.HANDLEUNKNOWN_VALUE;
		}

		return super.getHandleUnknown();
	}

	public Boolean getNormalize(){
		return getBoolean("normalize");
	}

	@SuppressWarnings("unchecked")
	public Map<Object, Map<Object, String>> getMinGroupCategories(){
		Map<Object, String> minGroupCategories = (Map)getDict("_min_group_categories");

		return CategoryEncoderUtil.toTransformedMap(minGroupCategories, key -> ScalarUtil.decode(key), value -> (Map<Object, String>)value);
	}

	private static final String HANDLEMISSING_COUNT = "count";
}