# Installation

## Requirements

- Python 3.9 or higher
- pip or poetry for package management

## Install from PyPI

```bash
pip install swifttrack
```

## Install with Development Dependencies

For contributing or running tests:

```bash
pip install swifttrack[dev]
```

This installs additional dependencies:
- pytest for testing
- ruff for linting
- black for formatting
- mypy for type checking
- mkdocs for documentation

## Verify Installation

```python
import swifttrack
print(swifttrack.__version__)
```

## From Source

To install from source for development:

```bash
git clone https://github.com/swifttrack/swifttrack-python.git
cd swifttrack-python
pip install -e .[dev]
```

## Troubleshooting

### Import Errors

If you see `ModuleNotFoundError`, ensure you've installed the package:

```bash
pip install swifttrack
```

### SSL Certificate Issues

If you encounter SSL errors, you can disable verification (not recommended for production):

```python
from swifttrack import SwiftTrackClient, SwiftTrackConfig

config = SwiftTrackConfig(verify_ssl=False)
client = SwiftTrackClient(config=config)
```

Or via environment variable:

```bash
export SWIFTTRACK_VERIFY_SSL=false
```
