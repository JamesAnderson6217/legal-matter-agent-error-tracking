package com.example.legal;

import java.time.LocalDate;

public final class DemoApplication {
  public static void main(String[] args) {
    String key = System.getenv("INFRAI_API_KEY");
    if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
    LegalMatterWorkflow workflow = new LegalMatterWorkflow(new InfraiClient(key));
    LegalMatterWorkflow.Matter matter = workflow.intake("Ada Lovelace", "employment agreement");
    System.out.println(matter.state());
    System.out.println(workflow.deliverSignedDocument(matter, false));
    System.out.println(workflow.followUpDeadline(matter, LocalDate.now().plusDays(3), LocalDate.now()));
  }
}
