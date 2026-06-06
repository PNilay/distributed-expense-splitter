package com.fairshare.distributed_expense_splitter.controller;

import org.junit.jupiter.api.Test;

public class WelcomeControllerTest {

//   WelcomeController controller;

  @Test
  void testGetWelcomeMessage() {
    System.out.println("Testing WelcomeController.getWelcomeMessage()...");
    WelcomeController controller = new WelcomeController();
    var response = controller.getWelcomeMessage();
    assert response.getStatusCode().is2xxSuccessful();
    assert response.getBody() != null;
    assert response
      .getBody()
      .getMessage()
      .equals("Welcome to Expense Splitter API");
  }
}
