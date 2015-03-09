/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.text.WordUtils;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.common.base.Preconditions;

import cern.colt.Arrays;
import eu.crisis_economics.abm.model.configuration.ComponentConfiguration;
import eu.crisis_economics.utilities.Pair;

/**
  * Static and stateless utility methods for the {@link MasterModel} subpackage.
  * This class provides the following static methods:
  * 
  * <ul>
  *   <li> {@link #search(Object, String, Class[])}<br>
  *        A depth first recursive parameter search tool. This method accepts an object
  *        {@code X}, the base name of a setter method, and an array of method argument
  *        types. Any object in the configuration hierarchy of {@code X} which has a 
  *        setter method with the appropriate signature and arguments is found and
  *        returned.
  *   <li> {@link #applyWithCloning(Object, String, Object...)}<br>
  *        This method invokes, with actual arguments, all methods identified by
  *        {@link #search(Object, String, Class[])}.
  *   <li> {@link #search(Object, String)} <br>
  *        A depth-first recursive parameter search tool. This method accepts an object
  *        {@code X} and the String ID of a {@link Parameter} to search for. 
  *        Any member field in the inheritance hierarchy of {@code X} or the inheritance 
  *        hierarchy of any member field in {@code X}, and so on, which carries a 
  *        {@link Parameter} annotation with the specified ID is returned. See also
  *        {@link #search(Object, String)}.
  * </ul>
  * 
  * @author phillips
  */
public final class ModelUtils {
   
   public static boolean
      VERBOSE_MODE = true;
   
   /**
     * A depth first recursive parameter search tool. This function accepts an object
     * {@code X}, the ({@link String}) name {@code N} of a method, and a {@link Class} array
     * of method argument types. Any object in the configuration hierarchy of {@code X} which
     * has a method with with the appropriate signature and arguments is found and
     * returned.<br>
     * 
     * This search operates as follows:
     * 
     * <ul>
     *   <li> If {@code X} contains a method {@code N} with the specified arguments, then 
     *        store and remember this method;
     *   <li> Otherwise search the subclasses of {@code X} for a method {@code N} with
     *        the specified arguments. If such a method is found, then store and
     *        remember this method;
     *   <li> Apply the above steps recursively (depth first) to every field in {@code X}
     *        of type {@link ComponentConfiguration}. Remember all of the methods identified
     *        by this search process and return these methods as well as the object 
     *        instances in which they were found.
     * </ul>
     * 
     * @param on
     *        The object to search.
     * @param methodToFind
     *        The method name to search for.
     * @param arguments
     *        A list of {@link Class} argument types for the method to find.
     * @return
     *        A {@link List} of {@link Pair}{@code s}. Each entry in this list is a {@link Pair}
     *        composed of one {@link Method} object and one {@link Object}. The {@link Method}
     *        satisfies the parameters of the query. The {@link Object} is an instance of an
     *        object whose class possesses the {@link Method}.
     */
   public static List<Pair<Method, Object>> search(
      final Object on,
      final String methodToFind,
      final Class<?>[] arguments
      ) {
      final List<Pair<Method, Object>>
         result = new ArrayList<Pair<Method, Object>>();
      final Class<?>
         parentType = on.getClass();
      for(Class<?> typeToSearch = parentType; 
         typeToSearch != null; 
         typeToSearch = typeToSearch.getSuperclass()
         ) {
         Method methodPtr = null;
         try {
            // Try to find a method with the specified name and exact argument types:
            methodPtr = typeToSearch.getDeclaredMethod(methodToFind, arguments);
            result.add(Pair.create(methodPtr, on));
            continue;
         }
         catch (final NoSuchMethodException e) {
            // Try to downcast method arguments for other class methods with the correct name:
            final Method[]
               allCalleeMethods = typeToSearch.getDeclaredMethods();
            for(final Method method : allCalleeMethods) {
               if(!method.getName().equals(methodToFind))
                  continue;
               final Type[]
                  argTypes = method.getGenericParameterTypes();
               if(argTypes.length != arguments.length)
                  continue;
               for(int j = 0; j< arguments.length; ++j) {
                  if(!arguments[j].isAssignableFrom(argTypes[j].getClass()))
                     continue;
               }
               methodPtr = method;
               result.add(Pair.create(methodPtr, on));
               continue;
            }
         }
         if(methodPtr == null)
            continue;
      }
      // Search for any ComponentConfiguration fields in the specified object:
      for(Class<?> typeToSearch = parentType; 
         typeToSearch != null; 
         typeToSearch = typeToSearch.getSuperclass()
         ) {
         for(Field field : typeToSearch.getDeclaredFields()) {
            if(!ComponentConfiguration.class.isAssignableFrom(field.getType()))
               continue;
            field.setAccessible(true);
            final Object
               instance;
            try {
               instance = field.get(on);
            } catch (final IllegalArgumentException e) {
               continue;                                                   // Not found
            } catch (final IllegalAccessException e) {
               continue;                                                   // Not found
            } 
            if (instance != null) {
               final List<Pair<Method, Object>>
                  subResult = search(instance, methodToFind, arguments);   // Descend into fields
               if(subResult != null && !subResult.isEmpty())
                  result.addAll(subResult);
               else continue;
            }
         }
      }
      return result;
   }
   
   public static class GetterSetterPair {
      private Method
         getter,
         setter;
      private Object
         instance;
      
      private GetterSetterPair(
         final Method getter,
         final Method setter,
         final Object instance
         ) {
         this.getter = Preconditions.checkNotNull(getter);
         this.setter = Preconditions.checkNotNull(setter);
         this.instance = Preconditions.checkNotNull(instance);
      }
      
      public Method getGetter() {
         return getter;
      }
      
      public Method getSetter() {
         return setter;
      }
      
      public Object getInstance() {
         return instance;
      }
   }
   
   public static List<GetterSetterPair> parameters(
      final Object of,
      final Class<?> paramType
      ) {
      final List<GetterSetterPair>
         result = new ArrayList<GetterSetterPair>();
      final Class<?>
         parentType = of.getClass();
      for(Class<?> typeToSearch = parentType; 
         typeToSearch != null; 
         typeToSearch = typeToSearch.getSuperclass()
         ) {
         final Map<String, Method>
            methodNames = new HashMap<String, Method>();
         final Method[]
            allCalleeMethods = typeToSearch.getDeclaredMethods();
         for(final Method method : allCalleeMethods) {
            final String
               name = method.getName();
            if(name.startsWith("get") &&
               ClassUtils.isAssignable(paramType, method.getReturnType(), true))
               methodNames.put(method.getName(), method);
            else if(name.startsWith("set") &&
               method.getParameterTypes().length == 1 &&
               ClassUtils.isAssignable(paramType, method.getParameterTypes()[0], true))
               methodNames.put(method.getName(), method);
            else continue;
            final String
               complement;
            if(name.startsWith("get")) {
               complement = "set" + name.substring(3);
               if(methodNames.containsKey(complement))
                  result.add(new GetterSetterPair(
                     method,
                     methodNames.get(complement),
                     of
                     ));
            }
            else if(name.startsWith("set")) {
               complement = "get" + name.substring(3);
               if(methodNames.containsKey(complement))
                  result.add(new GetterSetterPair(
                     methodNames.get(complement),
                     method,
                     of
                     ));
            }
         }
      }
      // Search for any ComponentConfiguration fields in the specified object:
      for(Class<?> typeToSearch = parentType; 
         typeToSearch != null; 
         typeToSearch = typeToSearch.getSuperclass()
         ) {
         for(Field field : typeToSearch.getDeclaredFields()) {
            if(!ComponentConfiguration.class.isAssignableFrom(field.getType()))
               continue;
            field.setAccessible(true);
            final Object
               instance;
            try {
               instance = field.get(of);
            } catch (final IllegalArgumentException e) {
               continue;                                                   // Not found
            } catch (final IllegalAccessException e) {
               continue;                                                   // Not found
            } 
            if (instance != null) {
               final List<GetterSetterPair>
                  subResult = parameters(instance, paramType);             // Descend into fields
               if(subResult != null && !subResult.isEmpty())
                  result.addAll(subResult);
               else continue;
            }
         }
      }
      return result;
   }
   
   /**
     * This method supplements {@link #search(Object, String, Class[])} by explicitly 
     * calling each of the {@link Method}{@code s} identified by {@code search} with 
     * concrete arguments.<br><br>
     * 
     * For each element in the return value of {@link #search(Object, String, Class[])}, this
     * method calls {@link Method#invoke(Object, Object...)} exactly once. The first argument
     * of this call is the object instance identified by {@code search}. The second (varargs) 
     * argument of this call is the list of parameters specified by the third argument of this
     * method.<br><br>
     * 
     * By default, every {@link Serializable} {@code argument} supplied to this method will
     * be cloned for each {@link Method} invocation.
     * 
     * @param on
     *        The object to configure.
     * @param setterName
     *        The base name of a setter method to search for. This method will be 
     *        converted to @{code set + capitalize(N)} if it does not already have
     *        this format.
     * @param arguments
     *        A list of arguments to invoke methods with the above name.
     * @return
     *        A list containing the return values of each call to
     *        {@link Method#invoke(Object, Object...)} made by this function.
     */
   public static List<Object> applyWithCloning(
      final Object on,
      final String setterName,
      final Object... arguments
      ) {
      return ModelUtils.apply(on, true, setterName, arguments);
   }
   
   /**
     * See {@link #applyWithCloning(Object, String, Object...)}.<br><br> This method supplements 
     * {@link #applyWithCloning(Object, String, Object...)} with the boolean argument
     * {@code doCloneArguments}.
     * 
     * @param doCloneArguments
     *        If <code>true</code>, all {@link Serializable} {@code arguments} will
     *        be cloned anew for each use. Otherwise, if <code>false</code>, the objects
     *        listed in {@code arguments} will not be cloned and may potentially be 
     *        reused for several {@link Method} invocations.
     */
   public static List<Object> apply(
      final Object on,
      final boolean doCloneArguments,
      final String setterName,
      final Object... arguments
      ) {
      final Class<?>[]
         argTypes = new Class<?>[arguments.length];
      for(int i = 0; i< arguments.length; ++i)
         argTypes[i] = arguments[i].getClass();
      final List<Pair<Method, Object>>
         targets = search(on, setterName, argTypes);
      final List<Object>
         result = new ArrayList<Object>();
      for(final Pair<Method, Object> target : targets)
         try {
            final Object[]
               argumentsToUse = new Object[arguments.length];
            for(int i = 0; i< arguments.length; ++i)
               if(arguments[i] instanceof Serializable && doCloneArguments)
                  argumentsToUse[i] = SerializationUtils.clone((Serializable) arguments[i]);
               else
                  argumentsToUse[i] = arguments[i];
            result.add(target.getFirst().invoke(target.getSecond(), argumentsToUse));
            if(VERBOSE_MODE)
               System.out.printf("Applying %s(%s) to %s\n",
                  target.getFirst().getName(),
                  arguments.length == 0 ? "void" :
                     arguments.length == 1 ? arguments[0].toString() :
                        Arrays.toString(arguments),
                  target.getFirst().getDeclaringClass().getSimpleName()
                  );
         } catch (final IllegalArgumentException e) {
            throw new IllegalStateException();
         } catch (final IllegalAccessException e) {
            throw new IllegalStateException();
         } catch (final InvocationTargetException e) {
            throw new IllegalStateException();
         }
      return result;
   }
   
   /**
     * Convert the basename {@code B} of a method to a full setter-type method name.<br><br>
     * This method is equivalent to the following:
     * 
     * <ul>
     *   <li> If {@code B} has the format {@code setX} where {@code X}
     *        is a {@link String} beginning with an uppercase letter, return {@code B};
     *   <li> Otherwise return {@code "set" + capitalize(B)}.
     * </ul>
     * 
     * @param baseName
     *        The {@link String} {@code B} above. This argument should be 
     *        non-<code>null</code> and nonempty.
     */
   public static String asSetter(final String baseName) {
      if(Preconditions.checkNotNull(baseName).isEmpty())
         throw new IllegalArgumentException();
      if(baseName.length() > 3 &&
         baseName.startsWith("set") && 
         Character.isUpperCase(baseName.charAt(3)) 
         ) return baseName;
      else
         return "set" + WordUtils.capitalize(baseName);
   }
   
   /**
     * Convert the basename {@code B} of a method to a full getter-type method name.<br><br>
     * This method is equivalent to the following:
     * 
     * <ul>
     *   <li> If {@code B} has the format {@code getX} where {@code X}
     *        is a {@link String} beginning with an uppercase letter, return {@code B};
     *   <li> Otherwise return {@code "get" + capitalize(B)}.
     * </ul>
     * 
     * @param baseName
     *        The {@link String} {@code B} above. This argument should be 
     *        non-<code>null</code> and nonempty.
     */
   public static String asGetter(final String baseName) {
      if(Preconditions.checkNotNull(baseName).isEmpty())
         throw new IllegalArgumentException();
      if(baseName.length() > 3 &&
         baseName.startsWith("get") && 
         Character.isUpperCase(baseName.charAt(3)) 
         ) return baseName;
      else
         return "get" + WordUtils.capitalize(baseName);
   }
   

   /**
     * A depth-first recursive parameter search tool. This function accepts an object
     * ({@code X}) and a {@link String} ID ({@code P}) of a parameter to search for.<br><br>
     * 
     * Any object {@code O} in the configuration hierarchy of {@code X} which possesses a field with
     * a) the appropriate annotation and b) parameter ID is identified and returned.
     * This method will search for member fields {@code F} (with any modifer) which satisfy:
     * 
     * <ul>
     *   <li> {@code F} carries a {@link Parameter} {@link Annotation}.
     *   <li> The {@code ID} of this {@link Parameter} is equal to {@code P}.
     *   <li> {@code F} does not itself belongs to a {@link Submodel} field that satisfies the
     *        above two conditions.
     * </ul>
     * 
     * This search operates as follows:<br><br>
     * 
     * <ul>
     *   <li> If {@code X} contains a member field {@code F} satisfying the above conditions, 
     *        {@code F} is accepted and returned.
     *   <li> Otherwise, each supertype in the inheritance hierarchy of {@code X} is searched.
     *   <li> Apply the above steps recursively (depth-first) to every field {@code F}
     *        in {@code X} annotated with {@link Submodel}, unless {@code F} itself
     *        satisfies the above conditions, in which case {@code F} is accepted and returned.
     * </ul>
     * 
     * This method returns a {@link List} of {@link ConfigurationModifier} objects. Each element
     * in this list corresponds to a field {@code F} somewhere in the inheritance hierarchy of
     * {@code X} which satisfied the above search conditions. {@link ConfigurationModifier}
     * objects facilitate direct changes to the value of each such {@code F}.<br><br>
     * 
     * @param on (<code>X</code>) <br>
     *        The object to search.
     * @param parameterIdToFind on (<code>P</code>) <br>
     *        The ID of the {@link Parameter} to search for.
     */
   public static List<ConfigurationModifier> search(
      final Object on,
      final String parameterIdToFind
      ) {
      if(on == null)
         throw new IllegalArgumentException("search: object to search is null.");
      if(parameterIdToFind == null || parameterIdToFind.isEmpty())
         throw new IllegalArgumentException("search: parameter name is empty or null.");
      if(VERBOSE_MODE)
         System.out.println("search: searching object " + 
            on.getClass().getSimpleName() + on.hashCode() + " for parameters of type " +
            parameterIdToFind + ".");
      final Class<?>
         objClass = on.getClass();
      final List<ConfigurationModifier>
         methodsIdentified = new ArrayList<ConfigurationModifier>();
      for(
         Class<?> typeToSearch = objClass; 
         typeToSearch != null; 
         typeToSearch = typeToSearch.getSuperclass()
         ) {
         for(final Field field : typeToSearch.getDeclaredFields()) {
            field.setAccessible(true);
            if(VERBOSE_MODE)
               System.out.println("inspecting field with name: " + field.getName() + ".");
            try {
               Annotation
                  drilldownAnnotation = null,
                  modelParameterAnnotation = null;
               for(final Annotation element : field.getAnnotations()) {
                  if(element.annotationType().getName() 
                     == Submodel.class.getName()) {            // Proxies
                     drilldownAnnotation = element;
                     if(VERBOSE_MODE)
                        System.out.println("field " + field.getName() + " is a subconfiguration.");
                     continue;
                  }
                  else if(element.annotationType().getName() 
                     == Parameter.class.getName()) {      // Proxies
                     final Class<? extends Annotation>
                        type = element.annotationType();
                     final String
                        id = (String) type.getMethod("ID").invoke(element);
                     if(parameterIdToFind.equals(id)) {
                        modelParameterAnnotation = element;
                        if(VERBOSE_MODE)
                           System.out.println("* field is valid.");
                        continue;
                     }
                     else
                        if(VERBOSE_MODE)
                           System.out.println(
                              "field ID [" + id + "] does not match the required ID: " 
                             + parameterIdToFind + ".");
                     continue;
                  }
                  else continue;
               }
               if(modelParameterAnnotation != null) {
                  final ConfigurationModifier
                     fieldWithMutators = findGetterSetterMethods(field, on, parameterIdToFind);
                  methodsIdentified.add(fieldWithMutators);
                  continue;
               }
               else if(drilldownAnnotation != null) {
                  if(VERBOSE_MODE)
                     System.out.println("descending into subconfiguration: " 
                      + field.getName());
                  final Object
                     fieldValue = field.get(on);
                  methodsIdentified.addAll(search(fieldValue, parameterIdToFind));
                  continue;
               }
               if(VERBOSE_MODE)
                  System.out.println("rejecting parameter: " + field.getName());
            } catch (final SecurityException e) {
               throw new IllegalStateException(
                  "search: a security exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            } catch (final IllegalArgumentException e) {
               throw new IllegalStateException(
                  "search: an illegal argument exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            } catch (final IllegalAccessException e) {
               throw new IllegalStateException(
                  "search: a security exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            } catch (final InvocationTargetException e) {
               throw new IllegalStateException(
                  "search: an invokation target exception was raised when testing a field with"
                + " name " + field.getName() + " for model parameter annotations. Details follow: "
                + e.getMessage() +".");
            } catch (final NoSuchMethodException e) {
               throw new IllegalStateException(
                  "search: a missing-method exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            }
         }
      }
      if(VERBOSE_MODE)
         System.out.println("searched: " + 
            on.getClass().getSimpleName() + on.hashCode() + " for parameters of type " +
            parameterIdToFind + ".");
      return methodsIdentified;
   }
   
   /**
     * Modify every instance of a parameter in the hierarchy of a configurator.
     * 
     * @param on <br>
     *        A configurator to modify.
     * @param parameterIdToFind <br>
     *        The name of a parameter to search for.
     * @param substitute (<code>V</code>) <br>
     *        The substitute (new value) to apply, if and when an instance of the parameter
     *        is found.
     * @param doClone <br>
     *        Whether or not <code>V</code> should be cloned for each substitution applied.
     *        If this argument is <code>true</code>, <code>V</code> must be an instance of
     *        {@link Serializable}.
     */
   public static void apply(
      Object on,
      final String parameterIdToFind,
      Object substitute,
      boolean doClone
      ) {
      final List<ConfigurationModifier>
         results = search(on, parameterIdToFind);
      for(ConfigurationModifier modifier : results) {
         if(doClone)
            modifier.set(SerializationUtils.clone((Serializable) substitute));
         else
            modifier.set(substitute);
      }
   }
   
   private static ConfigurationModifier findGetterSetterMethods(
      final Field field,
      final Object classInstance,
      final String parameterID
      ) throws SecurityException, NoSuchMethodException
      {
      final Class<?>
         type = classInstance.getClass();
      final String
         fieldName = field.getName(),
         expectedGetterName = "get" + WordUtils.capitalize(fieldName),
         expectedSetterName = "set" + WordUtils.capitalize(fieldName);
      final Method
         getterMethod = type.getMethod(expectedGetterName),
         setterMethod = type.getMethod(expectedSetterName, field.getType());
      return new ConfigurationModifier(
         field, 
         getterMethod,
         setterMethod,
         classInstance,
         parameterID
         );
   }
   
   /**
     * A lightweight aggregation class for 
     * 
     * @author phillips
     */
   public static final class ConfigurationModifier {
      
      @SuppressWarnings("unused")
      private final Field
         field;
      private final Method
         getter;
      private final Method
         setter;
      private final Object
         instance;
      private final String
         parameterId;
      
      private ConfigurationModifier(
         Field field,
         Method getter,
         Method setter,
         Object instance,
         String parameterId
         ) {
         super();
         this.field = Preconditions.checkNotNull(field);
         this.getter = Preconditions.checkNotNull(getter);
         this.setter = Preconditions.checkNotNull(setter);
         this.instance = Preconditions.checkNotNull(instance);
         this.parameterId = Preconditions.checkNotNull(parameterId);
      }
      
      public Object get() {
         try {
            return getter.invoke(instance);
         } catch (final IllegalArgumentException neverThrows) {
            throw new IllegalStateException(
               getClass().getSimpleName() + ": an unexpected illegal argument exception was raised"
               + " when a method with name " + getter.getName() + " was invoked with no arguments. "
               + "Details follows: " + neverThrows.getMessage());
         } catch (final IllegalAccessException e) {
            throw new IllegalStateException(
               getClass().getSimpleName() + ": a security exception was raised when a method with "
               + "name " + getter.getName() + " was invoked with no arguments. Details follows: "
               + e.getMessage());
         } catch (final InvocationTargetException e) {
            throw new IllegalStateException(
               getClass().getSimpleName() + ": a invokation target exception was raised when a"
               + " method with name " + getter.getName() + " was invoked with no arguments. "
               + "Details follows: " + e.getMessage());
         }
      }
      
      public void set(final Object setting) {
         try {
            setter.invoke(instance, setting);
         } catch (final IllegalArgumentException e) {
            throw new IllegalStateException(
               getClass().getSimpleName() + ": an illegal argument exception was raised"
               + " when a method with name " + setter.getName() + " was invoked with argument "
               + setting + ". Details follows: " + e.getMessage());
         } catch (final IllegalAccessException e) {
            throw new IllegalStateException(
               getClass().getSimpleName() + ": a security exception was raised when a method"
               + " with name " + setter.getName() + " was invoked with argument "
               + setting + ". Details follows: " + e.getMessage());
         } catch (final InvocationTargetException e) {
            throw new IllegalStateException(
               getClass().getSimpleName() + ": an invokation target exception was raised when a"
               + " method with name " + setter.getName() + " was invoked with argument "
               + setting + ". Details follows: " + e.getMessage());
         }
      }
      
      /**
        * Get the {@link String} ID of the parameter this class modifies.
        */
      public String getParameterID() {
         return parameterId;
      }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "Field With Mutators, parameter ID: " + parameterId + ".";
      }
   }
   
   /**
     * Get a list of subconfiguration objects for a class instance (<code>X</code>). This 
     * method will do the following:
     * 
     * <ul>
     *   <li> Identify any fields in the input <code>X</code> which carry the
     *        <code>@Parameter</code> and <code>@Submodel</code> annotations.
     *   <li> If <code>X</code> is an instance of {@link ComponentConfiguration}, and
     *        the ID of the accompanying <code>@Parameter</code> annotation is nonempty,
     *        set the scope string of the field to the parameter ID.
     *   <li> Add a reference to the field to a list <code>L</code>.
     *   <li> Repeat the above steps depth-first recursively for each field in <code>X</code>.
     *   <li> Return <code>L</code>
     * </ul>
     * 
     * @param on <br>
     *        The class instance <code>X</code> to search.
     */
   public static List<ComponentConfiguration> getSubconfigurators(
      final Object on
      ) {
      final Class<?>
         objClass = Preconditions.checkNotNull(on).getClass();
      final List<ComponentConfiguration>
         result = new ArrayList<ComponentConfiguration>();
      for(
         Class<?> typeToSearch = objClass; 
         typeToSearch != null; 
         typeToSearch = typeToSearch.getSuperclass()
         ) {
         for(final Field field : typeToSearch.getDeclaredFields()) {
            if(!ComponentConfiguration.class.isAssignableFrom(field.getType()))
               continue;
            field.setAccessible(true);
            try {
               Annotation
                  drilldownAnnotation = null,
                  modelParameterAnnotation = null;
               for(final Annotation element : field.getAnnotations()) {
                  if(element.annotationType().getName() 
                     == Submodel.class.getName()) {            // Proxies
                     drilldownAnnotation = element;
                     continue;
                  }
                  else if(element.annotationType().getName() 
                     == Parameter.class.getName()) {           // Proxies
                     modelParameterAnnotation = element;
                     continue;
                  }
                  else continue;
               }
               if(modelParameterAnnotation != null &&
                  drilldownAnnotation != null) {
                  final Object
                     value = field.get(on);
                  if(value == null)
                     throw new IllegalStateException(
                        on.getClass().getSimpleName() + ": the value of a configurator member field"
                     + " in " + (on.getClass().getSimpleName() + on.hashCode()) + " is null."
                       );
                  result.add((ComponentConfiguration) value);
                  final boolean
                     isScoped = (Boolean) modelParameterAnnotation.annotationType()
                        .getMethod("Scoped").invoke(modelParameterAnnotation);
                  if(!isScoped)
                     continue;
                  final String
                     id = (String) modelParameterAnnotation.annotationType()
                        .getMethod("ID").invoke(modelParameterAnnotation);
                  if(id.isEmpty())
                     continue;
                  ((ComponentConfiguration) value).setScope(id);
               }
            } catch (final SecurityException e) {
               throw new IllegalStateException(
                  on.getClass().getSimpleName() + 
                  ": a security exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            } catch (final IllegalArgumentException e) {
               throw new IllegalStateException(
                  on.getClass().getSimpleName() + 
                  "search: an illegal argument exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            } catch (final IllegalAccessException e) {
               throw new IllegalStateException(
                  on.getClass().getSimpleName() + 
                  "search: a security exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            } catch (final InvocationTargetException e) {
               throw new IllegalStateException(
                  on.getClass().getSimpleName() + 
                  "search: an invokation target exception was raised when testing a field with"
                + " name " + field.getName() + " for model parameter annotations. Details follow: "
                + e.getMessage() +".");
            } catch (final NoSuchMethodException e) {
               throw new IllegalStateException(
                  on.getClass().getSimpleName() + 
                  "search: a missing-method exception was raised when testing a field with name "
                + field.getName() + " for model parameter annotations. Details follow: " +
                  e.getMessage() +".");
            }
         }
      }
      return result;
   }
}
