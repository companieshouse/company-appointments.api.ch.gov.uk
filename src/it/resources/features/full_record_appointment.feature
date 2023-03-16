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