package com.nike.mm.rest.constraint

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import com.nike.mm.facade.impl.MeasureMentorRunFacade;
import com.nike.mm.rest.validation.constraint.ConfigValidator;

import spock.lang.Specification;

class ConfigValidatorUnitSpec extends Specification {

	private static final String MESSAGE_REFUSED_BY_FACADE = "message refused by facade"
	
	MeasureMentorRunFacade measureMentorRunFacade = Mock();
	ConstraintValidatorContext context = Mock();
	ConstraintViolationBuilder violationBuilder = Mock();
	Config config = Mock();
	
	ConfigValidator validator = new ConfigValidator(); 
	
	def setup() {
		validator.measureMentorRunFacade =  measureMentorRunFacade;
	}
	
	def "reject null configuration"() {
		when: 
			boolean isValid = validator.isValid(null, context);
		then:
			(1.._) * context.buildConstraintViolationWithTemplate({message -> message == ConfigValidator.CONFIGURATION_MANDATORY}) >> violationBuilder;
			1 * context.disableDefaultConstraintViolation()
			isValid == false;
	}
	
	def "reject single configuration with no type field"() {
		setup:
			config.type >> null; 
		when:
			boolean isValid = validator.isValid(config, context);
		then:
			(1.._) * context.buildConstraintViolationWithTemplate({message -> message == ConfigValidator.TYPE_FIELD_MANDATORY}) >> violationBuilder;
			1 * context.disableDefaultConstraintViolation()
			isValid == false;
	}
	
	def "reject single configuration invalidated by facade"() {
		setup:
			config.type >> "plugin";
			measureMentorRunFacade.validateConfig(_) >> MESSAGE_REFUSED_BY_FACADE;
		when:
			boolean isValid = validator.isValid(config, context);
		then:
			(1.._) * context.buildConstraintViolationWithTemplate({message -> message == MESSAGE_REFUSED_BY_FACADE}) >> violationBuilder;
			1 * context.disableDefaultConstraintViolation()
			isValid == false;
	}
	
	def "Accept single configuration validated by facade"() {
		setup:
			config.type >> "plugin";
			measureMentorRunFacade.validateConfig(_) >> null;
		when:
			boolean isValid = validator.isValid(config, context);
		then:
			0 * context.buildConstraintViolationWithTemplate({message -> message == MESSAGE_REFUSED_BY_FACADE}) >> violationBuilder;
			0 * context.disableDefaultConstraintViolation()
			isValid == true;
	}
	
	def "reject multiple configuration with no type field"() {
		setup:
			config.type >> null;
		when:
			boolean isValid = validator.isValid([config], context);
		then:
			(1.._) * context.buildConstraintViolationWithTemplate({message -> message == ConfigValidator.TYPE_FIELD_MANDATORY}) >> violationBuilder;
			1 * context.disableDefaultConstraintViolation()
			isValid == false;
	}
	
	def "reject multiple configuration invalidated by facade"() {
		setup:
			config.type >> "plugin";
			measureMentorRunFacade.validateConfig(_) >> MESSAGE_REFUSED_BY_FACADE;
		when:
			boolean isValid = validator.isValid([config], context);
		then:
			(1.._) * context.buildConstraintViolationWithTemplate({message -> message == MESSAGE_REFUSED_BY_FACADE}) >> violationBuilder;
			1 * context.disableDefaultConstraintViolation()
			isValid == false;
	}
	
	def "Accept multiple configuration validated by facade"() {
		setup:
			config.type >> "plugin";
			measureMentorRunFacade.validateConfig(_) >> null;
		when:
			boolean isValid = validator.isValid([config], context);
		then:
			0 * context.buildConstraintViolationWithTemplate({message -> message == MESSAGE_REFUSED_BY_FACADE}) >> violationBuilder;
			0 * context.disableDefaultConstraintViolation()
			isValid == true;
	}
}
