## MODIFIED Requirements

### Requirement: Send Email button visual design
The Send Email button SHALL be rendered as a prominent Material-style contained button using the primary brand colour (`#1976D2`), full-width within its container, with a paper-plane emoji prefix (✉️), uppercase label text "SEND TO ADMINISTRATION", an 8dp box shadow, and a hover state that darkens to `#1565C0`. The button MUST replace the previous small green `btn-send` style entirely.

#### Scenario: Send button appearance in issue card
- **WHEN** an ADMINISTRATOR views a PREPARED issue card that has not been sent
- **THEN** the Send Email button is displayed as a full-width blue contained button with emoji prefix and uppercase label

#### Scenario: Send button appearance in detail modal
- **WHEN** an ADMINISTRATOR opens the detail modal for a PREPARED issue that has not been sent
- **THEN** the Send Email button in the Admin Actions section matches the same full-width blue contained style with emoji prefix

#### Scenario: Send button disabled state after send
- **WHEN** an issue has already been sent (`isSend = true`)
- **THEN** the Send Email button is replaced by a "✅ SENT" badge and is not shown again
