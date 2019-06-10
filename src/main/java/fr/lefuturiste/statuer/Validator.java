package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.models.Service;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class Validator<T> {

    private final Set<ConstraintViolation<T>> validationResult;

    public Validator(T model) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        javax.validation.Validator validator = factory.getValidator();
        validationResult = validator.validate(model);
    }

    public boolean isValid() {
        return validationResult.size() == 0;
    }

    public JSONObject getJSONErrors() {
        JSONObject jsonObject = new JSONObject();
        for (ConstraintViolation<T> constraintViolation : validationResult) {
            String fieldName = ((PathImpl) constraintViolation.getPropertyPath()).getLeafNode().getName();
            if (!jsonObject.has(fieldName)) {
                jsonObject.put(fieldName, constraintViolation.getMessage());
            }
        }
        return jsonObject;
    }

}
