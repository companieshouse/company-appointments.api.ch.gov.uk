Feature: Process full record officer information

  Scenario Outline: Put an officer

    Given the company appointments api is running
    When I send a PUT request with payload "<Data>"
    Then I should receive a 200 status code
    And the record should be saved

    Examples:
    | Data                              |
    | natural_officer_full_record_PUT   |
    | corporate_officer_full_record_PUT |

  Scenario Outline: Get an officer

    Given the company appointments api is running
    When I send a GET request with id "<ID>"
    Then I should receive a 200 status code
    And the result should match "<Data>"

    Examples:
    | ID                          | Data                            |
    | 7IjxamNGLlqtIingmTZJJ42Hw9Q | natural_officer_full_record_GET |