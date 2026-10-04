package com.example.legal;

import java.time.LocalDate;

/** Teacherly example of three observable legal-tech agent steps. */
public final class LegalMatterWorkflow {
  private final InfraiClient infrai;

  public LegalMatterWorkflow(InfraiClient infrai) { this.infrai = infrai; }

  public Matter intake(String client, String matterType) {
    return new Matter(client, matterType, "INTAKE_REVIEW");
  }

  public String deliverSignedDocument(Matter matter, boolean signed) {
    if (!signed) {
      return capture("signed-document-delivery", "Document is awaiting signature for " + matter.client());
    }
    return "DELIVERED";
  }

  public String followUpDeadline(Matter matter, LocalDate dueDate, LocalDate today) {
    if (dueDate.isBefore(today)) {
      return capture("deadline-follow-up", "Deadline passed for " + matter.client());
    }
    return "FOLLOW_UP_SCHEDULED";
  }

  private String capture(String step, String message) {
    // InfraiClient.captureException is the reusable boundary for each agent step.
    return infrai.captureException(step + ": " + message);
  }

  public record Matter(String client, String matterType, String state) {}
}
