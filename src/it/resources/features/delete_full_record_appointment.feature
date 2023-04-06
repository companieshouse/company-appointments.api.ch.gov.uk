Feature: Delete full record officer information

    Scenario: Successfully delete a full-record appointment
      Given CHS kafka is available
      And the user is authenticated and authorised with internal app privileges
      When a request is sent to the DELETE endpoint to delete an officer
      Then a request is sent to the resource changed endpoint
      And the event type is "deleted"
      And the request body is a valid resource deleted request
      And the record should be deleted successfully

    Scenario: Record not found in database
      Given CHS kafka is available
      And the user is authenticated and authorised with internal app privileges
      And the record is not present in the database
      When a request is sent to the DELETE endpoint to delete an officer
      Then a request is sent to the resource changed endpoint
      And the event type is "deleted"
      And the request body is a valid resource deleted request
      And I should receive a 404 status code

    Scenario: User not authorised with internal app privileges
      Given CHS kafka is available
      And the user is not authenticated or authorised
      When a request is sent to the DELETE endpoint to delete an officer
      Then a request should NOT be sent to the resource changed endpoint
      And I should receive a 401 status code
      And the record should NOT be deleted

    Scenario: CHS kafka api unavailable
      Given CHS kafka is unavailable
      And the user is authenticated and authorised with internal app privileges
      When a request is sent to the DELETE endpoint to delete an officer
      And I should receive a 503 status code
      And the record should NOT be deleted
