# Production Planning & Tracking System (Project 2)
### Complete Code Explanation & Project Guide
**Assigned to:** Vaishnavi  
**Technology Stack:** Java (Micronaut Framework) + Vue.js 3 (Vite)

---

## 1. Project Overview & Objective
In manufacturing plants (such as automotive component manufacturing, machining, and assembly lines), production managers must set targets and monitor daily production to avoid shortages and delays.

This project achieves that goal by tracking:
- **Planned Target Quantity**: How many units were scheduled to be produced.
- **Actual Produced Quantity**: How many units were actually manufactured day-by-day across shifts.
- **Difference (Variance)**: The surplus or shortfall in production.
- **Achievement %**: The percentage of the planned target accomplished.

---

## 2. Core Business Logic & Mathematical Formulas

### Formula 1: Difference (Variance)
$$\text{Difference} = \text{Produced Quantity} - \text{Planned Quantity}$$
- **If Difference > 0 (Positive)**: Production exceeded the planned target (Surplus).
- **If Difference = 0**: Production exactly hit the target.
- **If Difference < 0 (Negative)**: Production fell short of the planned target (Deficit).

*Example:*  
- Planned Quantity = 500 units  
- Produced Quantity = 520 units  
- Difference = $520 - 500 = +20$ units (Ahead of schedule)

---

### Formula 2: Achievement Percentage
$$\text{Achievement \%} = \left(\frac{\text{Produced Quantity}}{\text{Planned Quantity}}\right) \times 100$$

*Example:*  
- Planned Quantity = 300 units  
- Produced Quantity = 250 units  
- Achievement % = $(250 / 300) \times 100 = 83.3\%$

---

### Thresholds & Status Rules
| Achievement % | Status Label | Visual Badge | Meaning |
| :--- | :--- | :--- | :--- |
| **$\ge 100\%$** | **Achieved** | 🟢 Green | Target met or exceeded |
| **$80.0\% - 99.9\%$** | **On Track** | 🟡 Yellow / Blue | Close to target, acceptable progress |
| **$< 80.0\%$** | **Lagging** | 🔴 Red | Behind target, needs immediate attention |

---

## 3. Architecture & Project Structure

```
production-planning-tracking/
│
├── backend/                       <-- Java Backend (Micronaut Framework)
│   ├── build.gradle               <-- Build configuration & dependencies
│   ├── gradlew.bat                <-- Gradle wrapper script
│   └── src/main/java/com/production/
│       ├── Application.java       <-- Main application entry point
│       ├── model/                 <-- Data objects (POJOs)
│       │   ├── Product.java
│       │   ├── ProductionPlan.java
│       │   ├── ProductionEntry.java
│       │   ├── VarianceReportItem.java
│       │   ├── LoginRequest.java
│       │   └── LoginResponse.java
│       ├── repository/
│       │   └── DataStore.java     <-- In-memory database with sample data
│       └── controller/            <-- REST API endpoints
│           ├── AuthController.java
│           ├── ProductController.java
│           ├── PlanController.java
│           ├── ProductionController.java
│           └── ReportController.java
│
├── frontend/                      <-- Frontend UI (Vue.js 3 + Vite)
│   ├── package.json               <-- Frontend dependencies
│   ├── vite.config.js             <-- Vite server & /api proxy
│   └── src/
│       ├── App.vue                <-- Main layout with top navbar
│       ├── main.js                <-- App initialization
│       ├── style.css              <-- Clean modern stylesheet
│       ├── router/index.js        <-- Routing & navigation guards
│       ├── services/api.js        <-- HTTP client calling backend
│       └── views/                 <-- 5 Required UI Screens
│           ├── LoginView.vue
│           ├── ProductMasterView.vue
│           ├── ProductionPlanningView.vue
│           ├── ProductionEntryView.vue
│           └── ProductionReportView.vue
│
└── PROJECT_EXPLANATION.md         <-- This documentation file
```

---

## 4. Backend (Micronaut Java) Detailed Explanation

### What is Micronaut?
Micronaut is a modern, lightweight Java framework. Unlike traditional Spring Boot which relies heavily on runtime reflection and proxies, Micronaut uses **compile-time ahead-of-time (AoT) processing**. This makes it:
1. Start in under a second.
2. Consume minimal memory.
3. Very easy to reason about because controllers and dependency injection are direct and straightforward.

### Key Backend Files:
1. **`Application.java`**:
   - Contains `public static void main(String[] args)`.
   - Starts the Micronaut Netty HTTP server on port `8080`.

2. **Models (`com.production.model`)**:
   - `Product`: Stores Product ID, Code (e.g. `PRD-001`), Name, Category, and Unit of Measure.
   - `ProductionPlan`: Stores Plan ID, Product ID, Target Month, Planned Quantity, and Notes.
   - `ProductionEntry`: Stores Log ID, Product ID, Date, Produced Quantity, Shift (`Morning`, `Evening`, `Night`), and Remarks.
   - `VarianceReportItem`: Holds the aggregated metrics (Planned Qty, Produced Qty, Difference, Achievement %, and Status).
   - `@Serdeable`: Annotation from `io.micronaut.serde.annotation.Serdeable` that enables automatic JSON serialization and deserialization without requiring complex configuration.

3. **`DataStore.java` (`com.production.repository`)**:
   - Annotated with `@Singleton` so Micronaut creates only one instance shared across all controllers.
   - Uses `CopyOnWriteArrayList` to ensure safe concurrent reading and writing.
   - Seeds realistic automotive manufacturing parts:
     - `PRD-001`: Alloy Wheel 17-inch
     - `PRD-002`: Engine Cylinder Block
     - `PRD-003`: Brake Rotor Disc
     - `PRD-004`: Steering Gearbox
   - `generateVarianceReport()`: Loops through each product, calculates total planned units, total actual produced units, computes the mathematical difference and achievement %, and returns the list.

4. **Controllers (`com.production.controller`)**:
   - **`AuthController`**: `@Post("/api/auth/login")` — validates login credentials (`admin` / `admin123` or `vaishnavi`).
   - **`ProductController`**:
     - `GET /api/products` — lists all products in master catalogue.
     - `POST /api/products` — saves a new product.
     - `DELETE /api/products/{id}` — removes a product.
   - **`PlanController`**:
     - `GET /api/plans` — lists production targets.
     - `POST /api/plans` — creates a new production target.
   - **`ProductionController`**:
     - `GET /api/production` — lists daily production logs.
     - `POST /api/production` — records actual manufactured units for a shift.
   - **`ReportController`**:
     - `GET /api/reports/variance` — returns the full variance report!

---

## 5. Frontend (Vue.js 3) Detailed Explanation

### 5 Required Screens:
1. **Login Screen (`LoginView.vue`)**:
   - Clean login card with prefilled helper (`admin` / `admin123`).
   - Authenticates via `POST /api/auth/login`, saves session to `localStorage`, and routes to `/reports`.
2. **Product Master Screen (`ProductMasterView.vue`)**:
   - Displays all registered products with code badges, categories, and units.
   - Includes real-time search bar and "+ Add New Product" modal form.
3. **Production Planning Screen (`ProductionPlanningView.vue`)**:
   - Displays scheduled production batches and total planned volume.
   - "+ Create Production Plan" modal lets user choose a product from a dropdown, set target period and quantity.
4. **Daily Production Entry Screen (`ProductionEntryView.vue`)**:
   - Displays daily production logs with Shift badges (Morning, Evening, Night).
   - "+ Record Production Entry" modal lets operators log manufactured quantity with date and comments.
5. **Production Summary & Variance Report Screen (`ProductionReportView.vue`)**:
   - **KPI Cards**:
     - Planned Quantity
     - Produced Quantity
     - Difference (+/-)
     - Overall Achievement %
   - **Variance Table**:
     - Product Code & Name
     - Planned Qty vs. Produced Qty
     - Difference
     - Achievement % with color-coded progress bars
     - Status badges: `Achieved`, `On Track`, `Lagging`
   - **Interactive Features**: Filter by status, search by keyword, Export to CSV, and Print.

---

## 6. How to Run the Application

### Running the Backend (Terminal 1):
```powershell
cd C:\Users\Vaishnavi\.gemini\antigravity\scratch\production-planning-tracking\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
.\gradlew.bat run
```
Backend will start on: **`http://localhost:8080`**

### Running the Frontend (Terminal 2):
```powershell
cd C:\Users\Vaishnavi\.gemini\antigravity\scratch\production-planning-tracking\frontend
npm run dev
```
Frontend will open on: **`http://localhost:5173`**

---

## 7. Viva & Interview Questions & Answers

**Q1: What is the primary purpose of this project?**  
**A:** To track manufacturing output against planned targets, enabling plant managers to immediately identify deficits (lagging products), measure achievement percentages, and balance production lines.

**Q2: Why did you choose Micronaut for the backend?**  
**A:** Micronaut provides high performance with zero runtime reflection. It compiles dependency injection at build time, resulting in fast startup times, low memory consumption, and clean, declarative controller annotations (`@Controller`, `@Get`, `@Post`).

**Q3: How is the variance / difference calculated?**  
**A:** `Difference = Produced Quantity - Planned Quantity`. A positive value indicates a surplus (ahead of schedule), zero means exactly on target, and a negative value indicates a shortfall.

**Q4: How does the system prevent division by zero in Achievement %?**  
**A:** If `Planned Quantity` is 0 or not yet set, the formula checks: if `plannedQuantity > 0`, calculate `(produced / planned) * 100`; otherwise, return 0% or 100% depending on whether any units were produced, avoiding an arithmetic crash.

**Q5: How does the Vue frontend communicate with the backend?**  
**A:** The frontend uses standard asynchronous `fetch` requests inside `services/api.js`. In development, Vite's reverse proxy forwards requests from `/api/*` to `http://localhost:8080/api/*`, eliminating Cross-Origin Resource Sharing (CORS) conflicts.
