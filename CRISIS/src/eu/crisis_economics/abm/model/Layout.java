/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 *
 * CRISIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CRISIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.crisis_economics.abm.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * An annotation for dashboard fields. This annotation has runtime retention
  * and applies to classes.<br><br>
  * 
  * This annotation adjusts the appearance and order of model class fields as they
  * are rendered on the dashbaord. In particular, this class allows the following
  * features to be customized:<br><br>
  * 
  * {@code (a)}
  *     the order in which dashboard fields are presented;<br>
  * {@code (b)} any banner text (text accompanied by a horizonal divider) immediately
  *     above the field;<br>
  * {@code (c)}
  *     a verbose textural description, rendered below the banner {@code (b)}, if
  *     applicable, but above the field itself;<br>
  * {@code (d)}
  *     the name of the field as it is displayed on the dashboard.<br><br>
  * 
  * Note that {@code (d)} allows the canoncial name of the model field to be overriden.
  * Concretely: if the name of the model field is {@code X}, and both the getter and 
  * mutator of {@code X} have the format {@code getX} {@code setX} respectively, then 
  * {@code (d)} overrides the name {@code X} and allows any other name to be attached
  * to the field.<br><br>
  * 
  * If a model class contains fields not annotated with {@link Layout}, then such
  * fields will still be rendered on the dashboard, however their mutual rendering
  * order is not specified. Such fields will typically not be accompanied by banners 
  * or verbose descriptions, will inherit field names as defined by their 
  * getter/mutator pairs, and will appear above any fields annotated with {@link Layout}.
  * 
  * @author phillips
  */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Layout {
   /**
     * Specify the rendering order for this field on the GUI. Negative values are
     * acceptable. The lowest values for {@link Order} will be rendered first.
     */
   double Order() default Double.MIN_VALUE;
   
   /**
     * Specify any banner text that must appear immediately above the field. Banners
     * are composed of a text field and a horizontal divider line.
     */
   String Title() default "";
   
   /**
     * Specify any verbose description that should accompany the field when it is 
     * rendered on the dashboard. Verbose descriptions are rendered above the field
     * but below any banner that may have been specified by {@link #Banner}.
     */
   String VerboseDescription() default "";
   
   /**
     * Specify the {@code URL} of any image to accompany this field on the GUI. If
     * this field is unspecified or is the empty string, no image is attached to 
     * the field.
     */
   String Image() default "";
   
   /**
     * Specify the name of the field as it is to be rendered on the GUI.
     */
   String FieldName() default "";
}