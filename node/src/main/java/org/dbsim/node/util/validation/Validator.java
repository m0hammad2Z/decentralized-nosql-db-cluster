package org.dbsim.node.util.validation;

import org.dbsim.node.exception.ValidationException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;


public class Validator {

    // -------- String Validation --------
    public static void isValidString(String str, String message, boolean required) {
        if ( (str == null || str.isEmpty() || str.isBlank())) {
            if (required) {
                throw new ValidationException(message);
            }
        }
    }


    // -------- Username Validation --------
    public static boolean isValidUsername(String username, boolean required) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            if (required) {
                throw new ValidationException("Username must not be null or empty");
            }
            return false;
        }
        return true;
    }


    // -------- Password Validation --------
    public static boolean isValidPassword(String password, boolean required) {
        if (password == null || password.isEmpty() || password.isBlank()) {
            if (required) {
                throw new ValidationException("Password must not be null or empty");
            }
            return false;
        }
        return true;
    }



    // -------- Null Validation --------
    public static void isNotNull(Object obj, String message , boolean required) {
        if (obj == null) {
            if (required) {
                throw new ValidationException(message);
            }
        }
    }

    // Check if the schema is a valid json schema
    public static boolean isValidSchema(JSONObject schema, boolean required) {
        if (schema == null) {
            if (required) {
                throw new ValidationException("Schema must not be null");
            }
            return false;
        }
        try {
            SchemaLoader.load(schema);
            return true;
        } catch (Exception e) {
            if (required) {
                throw new ValidationException("Invalid schema: " + e.getMessage());
            }
            return false;
        }
    }

        // Validate if the document is valid json and matches the schema
        public static boolean isValidDocument(JSONObject schema, JSONObject document, boolean required) {
            if (document == null) {
                if (required) {
                    throw new ValidationException("Document must not be null");
                }
                return false;
            }
            try {
                Schema jsonSchema = SchemaLoader.load(schema);
                jsonSchema.validate(document);
                return true;
            } catch (org.everit.json.schema.ValidationException e) {
                if (required) {
                    throw new ValidationException(e.getMessage());
                }
                return false;
            }
        }
    }
