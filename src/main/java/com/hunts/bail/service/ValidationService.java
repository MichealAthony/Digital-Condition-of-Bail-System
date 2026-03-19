package com.hunts.bail.service;

import com.hunts.bail.domain.AccusedPerson;
import com.hunts.bail.domain.BailCondition;
import com.hunts.bail.exception.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Stateless service that enforces all business validation rules
 *
 */
public class ValidationService {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$");

    private static final Pattern NATIONAL_ID_PATTERN =
            Pattern.compile("^[A-Z0-9]{6,15}$");

    // Public API 

    /**
     * Validates all fields required to create a new bail record
     * Collects ALL errors before throwing so the UI can show them together
     *
     * @throws ValidationException if any rule is violated
     */
    public void validateForCreate(AccusedPerson accused,
                                  List<BailCondition> conditions,
                                  String suretyInfo) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateAccusedPerson(accused));
        errors.addAll(validateConditions(conditions));

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Validates an AccusedPerson's fields.
     * Returns a list of error messages (empty = valid)
     */
    public List<String> validateAccusedPerson(AccusedPerson person) {
        List<String> errors = new ArrayList<>();

        if (person == null) {
            errors.add("Accused person details are required.");
            return errors;
        }
        if (isBlank(person.getFullName())) {
            errors.add("Accused person full name is required.");
        }
        if (person.getDateOfBirth() == null) {
            errors.add("Accused person date of birth is required.");
        } else if (person.getDateOfBirth().isAfter(LocalDate.now())) {
            errors.add("Date of birth cannot be in the future.");
        }
        if (isBlank(person.getNationalId())) {
            errors.add("National ID is required.");
        } else if (!NATIONAL_ID_PATTERN.matcher(person.getNationalId()).matches()) {
            errors.add("National ID format is invalid (6–15 uppercase alphanumeric characters).");
        }
        if (isBlank(person.getAddress())) {
            errors.add("Address is required.");
        }
        if (isBlank(person.getContactNumber())) {
            errors.add("Contact number is required.");
        } else if (!PHONE_PATTERN.matcher(person.getContactNumber()).matches()) {
            errors.add("Contact number format is invalid.");
        }
        if (person.getOffenses() == null || person.getOffenses().isEmpty()) {
            errors.add("At least one offense must be specified.");
        }

        return errors;
    }

    /**
     * Validates the list of bail conditions
     */
    public List<String> validateConditions(List<BailCondition> conditions) {
        List<String> errors = new ArrayList<>();

        if (conditions == null || conditions.isEmpty()) {
            errors.add("At least one bail condition must be specified.");
            return errors;
        }
        for (int i = 0; i < conditions.size(); i++) {
            BailCondition c = conditions.get(i);
            if (c.getConditionType() == null) {
                errors.add("Condition " + (i + 1) + ": type is required.");
            }
            if (isBlank(c.getDescription())) {
                errors.add("Condition " + (i + 1) + ": description is required.");
            }
        }
        return errors;
    }

    // Helpers 

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
