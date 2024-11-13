Feature: Upsert full record officer information

  Scenario Outline: Successfully Create a full-record appointment

    Given the company appointments api is running
    And CHS kafka is available
    And the user is authenticated and authorised with internal app privileges
    And the record does not already exist in the database
    When I send a PUT request with payload "<payloadFile>"
    Then a request is sent to the resource changed endpoint
    And the event type is "<eventType>"
    And the request body is a valid resource changed request
    And I should receive a 200 status code
    And the record should be saved

    Examples:
      | payloadFile                              | eventType |
      | natural_officer_full_record_PUT          | changed   |
      | corporate_officer_full_record_PUT        | changed   |

  Scenario Outline: Successfully Update a full-record appointment - most recent delta

    Given the company appointments api is running
    And CHS kafka is available
    And the user is authenticated and authorised with internal app privileges
    And the delta for payload "<payloadFile>" is the most recent delta for "<appointmentId>"
    When a request is sent to the PUT endpoint to upsert an officers delta
    Then a request is sent to the resource changed endpoint
    And the event type is "<eventType>"
    And the request body is a valid resource changed request
    And I should receive a 200 status code
    And the record should be saved

    Examples:
    | payloadFile                             | appointmentId               | eventType |
    | natural_officer_full_record_PUT         | 7IjxamNGLlqtIingmTZJJ42Hw9Q | changed   |
    | corporate_officer_full_record_PUT       | 7IjxamNGLlqtIingmTZJJ42Hw9Q | changed   |

  Scenario Outline: Update a full-record appointment - Stale delta

    Given CHS kafka is available
    And the user is authenticated and authorised with internal app privileges
    And the delta for payload "<payloadFile>" is a stale delta for "<appointmentId>"
    When a request is sent to the PUT endpoint to upsert an officers delta
    Then a request should NOT be sent to the resource changed endpoint
    And the changes within the delta for "<appointmentId>" should NOT be persisted in the database


    Examples:
      | payloadFile                        | appointmentId               |
      | natural_officer_full_record_PUT    | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
      | corporate_officer_full_record_PUT  | 7IjxamNGLlqtIingmTZJJ42Hw9Q |

  Scenario Outline: CHS Kafka api unavailable

    Given CHS kafka is unavailable
    And the user is authenticated and authorised with internal app privileges
    And the delta for payload "<payloadFile>" is the most recent delta for "<appointmentId>"
    When a request is sent to the PUT endpoint to upsert an officers delta
    Then I should receive a 502 status code
    And the record should be saved

    Examples:
      | payloadFile                        | appointmentId               |
      | natural_officer_full_record_PUT    | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
      | corporate_officer_full_record_PUT  | 7IjxamNGLlqtIingmTZJJ42Hw9Q |

  Scenario Outline: User not authorised or authenticated

    Given CHS kafka is available
    And the user is not authenticated or authorised
    And the delta for payload "<payloadFile>" is the most recent delta for "<appointmentId>"
    When a request is sent to the PUT endpoint to upsert an officers delta
    Then I should receive a 401 status code
    And a request should NOT be sent to the resource changed endpoint
    And the changes within the delta for "<appointmentId>" should NOT be persisted in the database

    Examples:
      | payloadFile                        | appointmentId               |
      | natural_officer_full_record_PUT    | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
      | corporate_officer_full_record_PUT  | 7IjxamNGLlqtIingmTZJJ42Hw9Q |

  Scenario Outline: User authenticated but not authorised so forbidden
    Given CHS kafka is available
    And the user is authenticated but not authorised
    And the delta for payload "<payloadFile>" is the most recent delta for "<appointmentId>"
    When a request is sent to the PUT endpoint to upsert an officers delta
    Then I should receive a 403 status code
    And a request should NOT be sent to the resource changed endpoint
    And the changes within the delta for "<appointmentId>" should NOT be persisted in the database

    Examples:
      | payloadFile                        | appointmentId               |
      | natural_officer_full_record_PUT    | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
      | corporate_officer_full_record_PUT  | 7IjxamNGLlqtIingmTZJJ42Hw9Q |