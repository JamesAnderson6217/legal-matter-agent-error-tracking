package com.example.legal;

import java.time.LocalDate;

public final class LegalMatterWorkflowTest {
  public static void main(String[] args) {
    LegalMatterWorkflow workflow = new LegalMatterWorkflow(new InfraiClient("test-key", "http://127.0.0.1:1"));
    LegalMatterWorkflow.Matter matter = workflow.intake("Test Client", "lease review");
    assert "INTAKE_REVIEW".equals(matter.state());
    assert "DELIVERED".equals(workflow.deliverSignedDocument(matter, true));
    assert "FOLLOW_UP_SCHEDULED".equals(workflow.followUpDeadline(matter, LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 1)));
    System.out.println("legal matter decisions passed");
  }
}
