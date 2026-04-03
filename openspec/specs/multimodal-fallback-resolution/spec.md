## Purpose

Define the multimodal fallback resolution behavior for selector resolution when JavaFX structural matching is unavailable.

## Requirements

### Requirement: OCR fallback resolution
The system SHALL perform OCR-based selector resolution when a requested element cannot be resolved from JavaFX structure and the selector can be matched by visible text.

#### Scenario: Click login button through OCR fallback
- **WHEN** a click action cannot resolve a JavaFX node and the screen contains visible text matching the requested selector
- **THEN** the system captures a screenshot, performs OCR, resolves the matching text region, and executes the click against that region

### Requirement: Vision fallback resolution
The system SHALL perform template-matching based resolution only after both JavaFX structural resolution and OCR text resolution fail.

#### Scenario: Match element by template after OCR fails
- **WHEN** a requested target cannot be resolved from JavaFX structure and OCR does not identify a matching text region
- **THEN** the system attempts vision template matching as the final fallback step before declaring the action unresolved

### Requirement: Deterministic fallback ordering
The system SHALL preserve a deterministic fallback chain of JavaFX structure first, OCR second, and vision third for every selector resolution attempt.

#### Scenario: Record fallback chain in action result
- **WHEN** an action resolves through vision matching
- **THEN** the action result shows that JavaFX and OCR resolution were attempted before the vision tier succeeded

### Requirement: Confidence reporting for fallback tiers
The system SHALL include a confidence score for OCR and vision-based resolutions in the action result and debug output.

#### Scenario: Return OCR confidence metadata
- **WHEN** a click action resolves through OCR text matching
- **THEN** the system returns the OCR confidence score together with the matched text region and selector used

### Requirement: Local performance bound for fallback execution
The system SHALL complete an OCR fallback resolution attempt within one second under the accepted Phase 1 local-machine operating conditions.

#### Scenario: OCR completes within acceptable bound
- **WHEN** the target screen state is stable and the automation engine invokes OCR fallback on a supported local machine
- **THEN** the OCR resolution step completes within one second before action dispatch or failure reporting
