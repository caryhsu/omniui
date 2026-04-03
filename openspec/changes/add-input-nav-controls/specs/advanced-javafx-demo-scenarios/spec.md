## ADDED Requirements

### Requirement: Advanced demo app extended with input and navigation controls
The demo application SHALL be extended with five new demo sections covering
RadioButton/ToggleButton, Slider/Spinner, ProgressBar, and TabPane controls,
each with stable fx:id attributes for automation targeting.

#### Scenario: New demo sections present in running application
- **WHEN** the demo application is running after this change
- **THEN** `get_nodes()` returns nodes with ids `radioToggleSection`,
  `sliderSpinnerSection`, `progressSection`, `tabSection`, and their
  child control nodes

#### Scenario: radio_toggle_demo.py passes
- **WHEN** `python demo/python/radio_toggle_demo.py` is run
- **THEN** the script exits with code 0, having read and set RadioButton and
  ToggleButton states

#### Scenario: slider_spinner_demo.py passes
- **WHEN** `python demo/python/slider_spinner_demo.py` is run
- **THEN** the script exits with code 0, having set Slider value and used
  Spinner increment/decrement

#### Scenario: progress_demo.py passes
- **WHEN** `python demo/python/progress_demo.py` is run
- **THEN** the script exits with code 0, having read ProgressBar and
  ProgressIndicator values

#### Scenario: tab_demo.py passes
- **WHEN** `python demo/python/tab_demo.py` is run
- **THEN** the script exits with code 0, having listed tabs and selected
  each tab by title
