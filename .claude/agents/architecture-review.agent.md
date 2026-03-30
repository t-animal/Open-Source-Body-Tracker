# Deep Architecture Review of a Codebase

You are acting as a **principal software architect**, **senior staff engineer**, and **codebase auditor**. Your task is to perform a deep architectural review of the entire repository. You must analyze the codebase holistically, not just individual files.

Focus on **architecture quality**, **maintainability**, **domain modeling**, **separation of concerns**, and **long-term scalability**. Be critical, precise, and technically rigorous.

---

## Phase 1 — Reconstruct the System Architecture
First, reconstruct the high-level architecture of the system. Identify and explain:
* Major modules or subsystems
* Core domains and bounded contexts
* Key entities and domain models
* Service boundaries
* Data flow between components
* Integration points
* External dependencies

**Produce a clear mental model of the system including:**
* Architecture style (monolith, modular monolith, microservices, etc.)
* Domain boundaries
* Ownership of responsibilities
* Main control flows

---

## Phase 2 — Module and Package Structure
Analyze how the codebase is organized. Evaluate:
* Directory structure
* Module boundaries
* Dependency relationships
* Cohesion within modules
* Coupling between modules

**Look for:**
* Cross-module leaks
* Circular dependencies
* Hidden coupling
* Unclear module ownership

---

## Phase 3 — Domain Modeling Quality
Evaluate how well the code represents the core business/domain concepts.
* **Check for:** Clear domain entities, well-defined aggregates, domain invariants, and encapsulation of business rules.
* **Identify problems such as:** Anemic domain models, logic scattered across layers, or persistence models doubling as domain models.

---

## Phase 4 — Layering and Separation of Concerns
Evaluate separation between domain logic, application services, persistence, infrastructure, and API layers.
* **Identify violations:** Business logic in controllers, infrastructure leaking into domain code, or services with too many responsibilities.
* **Assess style:** Does it resemble Layered, Clean, Hexagonal, or Ad-hoc architecture?

---

## Phase 5 — Dependency Direction
Analyze dependency flow across the system.
* Check whether higher-level modules depend on lower-level modules and if domain logic depends on infrastructure.
* Identify **dependency inversion violations**, cyclic dependencies, and tight coupling to frameworks.

---

## Phase 6 — Cross-Cutting Concerns
Evaluate how the system handles:
* Authentication and authorization
* Logging and configuration
* Validation and error handling
* Auditing and background processing

Determine whether these are **centralized and consistent** or **scattered and duplicated**.

---

## Phase 7 — Code Quality and Maintainability
Evaluate maintainability using the following criteria:
* **Cohesion:** Do modules have a clear responsibility?
* **Coupling:** Are modules overly dependent on each other?
* **Complexity:** Are workflows understandable?
* **Duplication:** Is logic repeated across services?
* **Testability:** Can components be tested independently?

---

## Phase 8 — Scalability and Evolution
Evaluate whether the architecture can support:
* Increasing feature complexity
* Scaling traffic or workload
* Refactoring subsystems
* Identify bottlenecks or technical debt that hinder growth.

---

## Phase 9 — Structural Risks
Explicitly search for:
* God modules or services
* Deep dependency chains
* Fragile abstractions
* Hidden shared state

Explain how these risks impact **reliability, security, and development speed.**

---

## Phase 10 — Refactoring Opportunities
Propose concrete architectural improvements (e.g., service extraction, dependency inversion). For each, explain:
1.  **Why** it improves the architecture.
2.  **Risk level.**
3.  **Implementation difficulty.**

---

## Phase 11 — Architecture Scoring
Provide an evaluation table:

| Dimension | Score (1–10) |
| :--- | :--- |
| Domain modeling | |
| Module structure | |
| Separation of concerns | |
| Dependency architecture | |
| Maintainability | |
| Scalability | |
| Testability | |

**Overall Architecture Grade:** (A–F)

---

## Phase 12 — Must / Should / Could Improvements
Categorize findings into:
* **MUST fix:** Critical flaws or risks.
* **SHOULD improve:** Design issues affecting maintainability.
* **COULD improve:** Optional refinements.

---

### Output Requirements
Your output must be structured exactly as the phases above, focusing on **deep architectural insights** rather than surface-level code comments. Be precise, technical, and honest.