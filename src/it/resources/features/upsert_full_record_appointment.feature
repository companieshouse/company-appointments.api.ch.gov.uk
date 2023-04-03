Feature: Process full record officer information

  Scenario Outline: Successfully Save a full-record appointment

    Given the company appointments api is running
    And CHS kafka is available
    And the record does not already exist in the database
    When I send a PUT request with payload "<payloadFile>"
    Then CHS kafka is invoked successfully
    And I should receive a 200 status code
    And the record should be saved

    Examples:
      | payloadFile                              |
      | natural_officer_full_record_PUT          |
      | corporate_officer_full_record_PUT        |

  Scenario Outline: Successfully Update a full-record appointment

    Given the company appointments api is running
    And CHS kafka is available
    And the delta for payload "<payloadFile>" is the most recent delta for "<companyNumber>" and "<appointmentId>"
    When I send a PUT request with payload "<payloadFile>"
    Then CHS kafka is invoked successfully
    And I should receive a 200 status code
    And the record should be saved

    Examples:
    | payloadFile                             | companyNumber | appointmentId               |
    | natural_officer_full_record_PUT         | 01777777      | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
    | corporate_officer_full_record_PUT       | 01777777      | 7IjxamNGLlqtIingmTZJJ42Hw9Q|

  Scenario Outline: Update a full-record appointment - Stale delta

    Given CHS kafka is available
    And the delta for payload "<payloadFile>" is a stale delta
    When I send a PUT request with payload "<payloadFile>"
    Then a request should NOT be sent to the resource changed endpoint
    And the changes within the delta should NOT be persisted in the database.


    Examples:
      | payloadFile                        |
      | natural_officer_full_record_PUT    |
      | corporate_officer_full_record_PUT  |

