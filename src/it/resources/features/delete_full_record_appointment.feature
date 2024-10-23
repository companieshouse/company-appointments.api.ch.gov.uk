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
      Given the user is authenticated and authorised with internal app privileges
      And CHS kafka is available
      And the record is not present in the delta_appointment database
      When a request is sent to the DELETE endpoint to delete an officer
      Then I should receive a 200 status code

    Scenario: User not authorised with internal app privileges
      Given the user is not authenticated or authorised
      When a request is sent to the DELETE endpoint to delete an officer
      Then a request should NOT be sent to the resource changed endpoint
      And I should receive a 401 status code
      And the record should NOT be deleted

   Scenario: User authenticated but not authorised so forbidden
      Given CHS kafka is available
      And the user is authenticated but not authorised
      When a request is sent to the DELETE endpoint to delete an officer
      Then a request should NOT be sent to the resource changed endpoint
      And I should receive a 403 status code
      And the record should NOT be deleted

    Scenario: CHS kafka api unavailable
      Given CHS kafka is unavailable
      And the user is authenticated and authorised with internal app privileges
      When a request is sent to the DELETE endpoint to delete an officer
      And I should receive a 503 status code
      And the record should be deleted successfully
