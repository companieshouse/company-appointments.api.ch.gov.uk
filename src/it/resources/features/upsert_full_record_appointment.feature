Feature: Process full record officer information

  Scenario Outline: Successfully Save a full-record appointment

    Given the company appointments api is running
    And CHS kafka is available
    And the record does not already exist in the database
    When I send a PUT request with payload "<Data>"
    Then CHS kafka is invoked successfully
    And I should receive a 200 status code
    And the record should be saved

    Examples:
      | Data                              | companyNumber | appointmentId               |
      | natural_officer_full_record_PUT   | 12345678      | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
      | corporate_officer_full_record_PUT | 01777777      | EcEKO1YhIKexb0cSDZsn_OHsFw4 |

  Scenario Outline: Successfully Update a full-record appointment

    Given the company appointments api is running
    And CHS kafka is available
    And the delta for payload "<Data>" is the most recent delta for "<companyNumber>" and "<appointmentId>"
    When I send a PUT request with payload "<Data>"
    Then CHS kafka is invoked successfully
    And I should receive a 200 status code
    And the record should be saved

    Examples:
    | Data                              | companyNumber | appointmentId               |
    | natural_officer_full_record_PUT   | 01777777      | 7IjxamNGLlqtIingmTZJJ42Hw9Q |
    | corporate_officer_full_record_PUT | 01777777      | 7IjxamNGLlqtIingmTZJJ42Hw9Q|

  Scenario Outline: Update a full-record appointment - Stale delta

    Given: CHS


    Examples:
      | Data                              |
      | natural_officer_full_record_PUT   |
      | corporate_officer_full_record_PUT |