---
name: feature-to-requirements
description: Runs the full feature pipeline - discovers the feature concept with the user, saves it to a file, then runs requirements elicitation based on that concept.
---

# Feature-to-Requirements Pipeline

You are orchestrating a two-phase product discovery and requirements pipeline. Follow these steps exactly.

## Phase 1: Feature Discovery

Launch the `feature-discovery` agent using the Agent tool (subagent_type: "feature-discovery"). Pass it the user's feature idea as the prompt. The agent will conduct an interactive dialogue with the user to refine the idea into a Feature Concept Document.

**Important**: The feature-discovery agent is interactive — it will ask the user questions and iterate. Wait for the agent to complete its full discovery process and return the final Feature Concept Document.

## Phase 2: Save the Feature Concept

Once the feature-discovery agent returns its result (containing the Feature Concept Document):

1. Create a timestamped markdown file at `documents/plans/YYYY-MM-DD-[short-name]-feature-concept.md` (create the `documents/plans/` directory if it doesn't exist).
2. Write the full Feature Concept Document to this file.
3. Tell the user where the concept was saved.
4. Wait until the user has acknowledged the saved concept and is ready to proceed to requirements engineering before moving on to the next phase. If the user wants to make edits to the concept, allow them to do so by going back to the feature-discovery agent before proceeding.

## Phase 3: Requirements Engineering

Launch the `requirements-engineer` agent using the Agent tool (subagent_type: "requirements-engineer"). In the prompt, include the full contents of the saved Feature Concept Document file, and instruct the agent to use it as the starting point for requirements elicitation.

**Important**: The requirements-engineer agent is also interactive — it will ask the user questions. Wait for it to complete.

## Phase 4: Save the Requirements

Once the requirements-engineer agent returns its result:

1. Save the final Feature Specification to `documents/plans/YYYY-MM-DD-[short-name]-requirements.md`.
2. Tell the user where both documents are saved and that the feature is ready for implementation planning.
3. Wait until the user has acknowledged the saved requirements and is ready to proceed to implementation planning before completing. If the user wants to make edits to the requirements, allow them to do so by going back to the requirements-engineer agent before proceeding.