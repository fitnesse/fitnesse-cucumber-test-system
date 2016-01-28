Feature: A fancy scenario outline

  Scenario Outline: squared numbers (last one fails)

    Given a variable x with value <value>
    When I multiply x by <value>
    Then x should equal <outcome>

    Examples:
      | value | outcome |
      | 2 | 4 |
      | 3 | 9 |
      | 4 | 10 |

  Scenario: 2 squared

    Given a variable x with value 2
    When I multiply x by 2
    Then x should equal 4