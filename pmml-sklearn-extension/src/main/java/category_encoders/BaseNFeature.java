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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.NormDiscrete;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.converter.BinaryThresholdFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.ExpressionUtil;
import org.jpmml.converter.Feature;
import org.jpmml.converter.FieldNameUtil;
import org.jpmml.converter.IfElseBuilder;
import org.jpmml.converter.PMMLEncoder;
import org.jpmml.model.ToStringHelper;

public class BaseNFeature extends BinaryThresholdFeature {

	private int base = -1;

	private int index = -1;

	// Inverse Map: keys are base values, values are categories
	private SetMultimap<Integer, ?> values = null;

	private Object missingCategory = null;

	private Integer defaultValue = null;


	public BaseNFeature(PMMLEncoder encoder, Field<?> field, int base, int index, SetMultimap<Integer, ?> values, Object missingCategory, Integer defaultValue){
		this(encoder, field.requireName(), field.requireDataType(), base, index, values, missingCategory, defaultValue);
	}

	public BaseNFeature(PMMLEncoder encoder, Feature feature, int base, int index, SetMultimap<Integer, ?> values, Object missingCategory, Integer defaultValue){
		this(encoder, feature.getName(), feature.getDataType(), base, index, values, missingCategory, defaultValue);
	}

	public BaseNFeature(PMMLEncoder encoder, String name, DataType dataType, int base, int index, SetMultimap<Integer, ?> values, Object missingCategory, Integer defaultValue){
		super(encoder, name, dataType);

		setBase(base);
		setIndex(index);

		setValues(values);
		setMissingCategory(missingCategory);
		setDefaultValue(defaultValue);
	}

	@Override
	public String getDerivedName(){
		return FieldNameUtil.create("base" + getBase(), getName(), getIndex());
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		String name = getName();
		DataType dataType = getDataType();
		int base = getBase();
		SetMultimap<Integer, ?> values = getValues();
		Object missingCategory = getMissingCategory();
		Integer defaultValue = getDefaultValue();

		boolean missingValueAware = (missingCategory != null && values.containsValue(missingCategory));

		if(defaultValue != null && defaultValue != 0){
			throw new IllegalArgumentException();
		}

		Supplier<Expression> expressionSupplier = () -> {
			Map<Integer, ? extends Collection<?>> valueMap = values.asMap();

			if(base == 2){
				Collection<?> categories = valueMap.get(1);

				if(categories != null && categories.size() == 1){
					Object category = Iterables.getOnlyElement(categories);

					if(!missingValueAware){
						return new NormDiscrete(name, category);
					}
				}
			}

			Integer missingBaseValue = 0;

			IfElseBuilder applyBuilder = new IfElseBuilder();

			Collection<? extends Map.Entry<Integer, ? extends Collection<?>>> entries = valueMap.entrySet();

			entries = entries.stream()
				.sorted((left, right) -> Integer.compare(left.getKey(), right.getKey()))
				.filter(entry -> (entry.getKey() > 0))
				.collect(Collectors.toList());

			entries:
			for(Map.Entry<Integer, ? extends Collection<?>> entry : entries){
				Integer baseValue = entry.getKey();
				Collection<?> categories = entry.getValue();

				if(missingValueAware){

					if(categories.contains(missingCategory)){
						categories.remove(missingCategory);

						missingBaseValue = baseValue;
					}
				} else

				{
					if(categories.contains(CategoryEncoder.CATEGORY_NAN)){
						categories.remove(CategoryEncoder.CATEGORY_NAN);
					}
				} // End if

				if(categories.isEmpty()){
					continue entries;
				}

				Apply valueApply = ExpressionUtil.createValueApply(new FieldRef(name), dataType, categories);

				applyBuilder.add(valueApply, ExpressionUtil.createConstant(baseValue));
			}

			if(applyBuilder.isEmpty()){

				if(missingBaseValue != 0){
					return ExpressionUtil.createApply(PMMLFunctions.IF,
						ExpressionUtil.createApply(PMMLFunctions.ISNOTMISSING, new FieldRef(name)),
						ExpressionUtil.createConstant(0),
						ExpressionUtil.createConstant(missingBaseValue)
					);
				}

				return ExpressionUtil.createConstant(0);
			} else

			{
				applyBuilder.terminate(ExpressionUtil.createConstant(0));

				Apply apply = applyBuilder.build();

				if(missingValueAware){
					apply = ExpressionUtil.createApply(PMMLFunctions.IF,
						ExpressionUtil.createApply(PMMLFunctions.ISNOTMISSING, new FieldRef(name)),
						apply,
						ExpressionUtil.createConstant(missingBaseValue)
					);
				}

				return apply;
			}
		};

		return toContinuousFeature(getDerivedName(), DataType.INTEGER, expressionSupplier);
	}

	@Override
	public Set<?> getValues(Predicate<Number> predicate){
		SetMultimap<Integer, ?> values = getValues();
		Integer defaultValue = getDefaultValue();

		// XXX
		if(defaultValue != null){
			throw new IllegalArgumentException();
		}

		Map<Integer, ? extends Collection<?>> valueMap = values.asMap();

		Set<Object> result = new LinkedHashSet<>();

		Set<? extends Map.Entry<Integer, ? extends Collection<?>>> entries = valueMap.entrySet();

		entries.stream()
			.sorted((left, right) -> Integer.compare(left.getKey(), right.getKey()))
			.filter((entry) -> predicate.test(entry.getKey()))
			.map((entry) -> entry.getValue())
			.forEach(result::addAll);

		return result;
	}

	@Override
	public int hashCode(){
		int result = super.hashCode();

		result = (31 * result) + Objects.hash(this.getBase());
		result = (31 * result) + Objects.hash(this.getIndex());
		result = (31 * result) + Objects.hash(this.getValues());
		result = (31 * result) + Objects.hash(this.getMissingCategory());
		result = (31 * result) + Objects.hash(this.getDefaultValue());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof BaseNFeature){
			BaseNFeature that = (BaseNFeature)object;

			return super.equals(object) && Objects.equals(this.getBase(), that.getBase()) && Objects.equals(this.getIndex(), that.getIndex()) && Objects.equals(this.getValues(), that.getValues()) && Objects.equals(this.getMissingCategory(), that.getMissingCategory()) && Objects.equals(this.getDefaultValue(), that.getDefaultValue());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("base", getBase())
			.add("index", getIndex())
			.add("values", getValues())
			.add("missingCategory", getMissingCategory())
			.add("defaultValue", getDefaultValue());
	}

	public int getBase(){
		return this.base;
	}

	private void setBase(int base){
		this.base = base;
	}

	public int getIndex(){
		return this.index;
	}

	private void setIndex(int index){
		this.index = index;
	}

	public SetMultimap<Integer, ?> getValues(){
		return this.values;
	}

	private void setValues(SetMultimap<Integer, ?> values){
		this.values = Objects.requireNonNull(values);
	}

	@Override
	public Object getMissingValue(){
		return getMissingCategory();
	}

	public Object getMissingCategory(){
		return this.missingCategory;
	}

	private void setMissingCategory(Object missingCategory){
		this.missingCategory = missingCategory;
	}

	public Integer getDefaultValue(){
		return this.defaultValue;
	}

	private void setDefaultValue(Integer defaultValue){
		this.defaultValue = defaultValue;
	}
}