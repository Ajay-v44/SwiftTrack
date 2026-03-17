# Contributing to SwiftTrack Python SDK

Thank you for your interest in contributing to the SwiftTrack Python SDK! This document provides guidelines and instructions for contributing.

## Development Setup

### 1. Clone the Repository

```bash
git clone https://github.com/swifttrack/swifttrack-python.git
cd swifttrack-python
```

### 2. Create Virtual Environment

```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

### 3. Install Dependencies

```bash
pip install -e .[dev]
```

### 4. Run Tests

```bash
pytest
```

## Development Workflow

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Write code following the existing style
- Add tests for new functionality
- Update documentation as needed

### 3. Run Quality Checks

```bash
# Format code
black src tests

# Lint
ruff check src tests

# Type check
mypy src

# Run tests
pytest
```

### 4. Commit Changes

```bash
git add .
git commit -m "feat: add new feature"
```

Follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `test:` Tests
- `refactor:` Code refactoring
- `chore:` Maintenance

### 5. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Create a Pull Request on GitHub.

## Code Style

### Python Style

- Follow PEP 8
- Use type hints
- Maximum line length: 100 characters
- Use black for formatting
- Use ruff for linting

### Example

```python
def get_address(self, address_id: UUID | str) -> Address:
    """Get a specific address by ID.

    Args:
        address_id: UUID of the address.

    Returns:
        Address object.

    Raises:
        NotFoundError: If address doesn't exist.
    """
    address_uuid = UUID(address_id) if isinstance(address_id, str) else address_id
    response = self._client.get(f"{self.BASE_PATH}/{address_uuid}")
    return Address.model_validate(response)
```

## Testing

### Write Tests

All new code must have tests. We use pytest and respx for mocking.

```python
def test_get_address(self, client: SwiftTrackClient) -> None:
    """Test getting a specific address."""
    # Arrange
    address_id = uuid.uuid4()
    respx.get(f"...").mock(return_value=Response(200, json={...}))

    # Act
    address = client.addresses.get_address(address_id)

    # Assert
    assert isinstance(address, Address)
    assert address.id == address_id
```

### Run Tests

```bash
# All tests
pytest

# With coverage
pytest --cov=swifttrack

# Specific test file
pytest tests/test_auth.py

# Specific test
pytest tests/test_auth.py::TestAuthService::test_login_success
```

## Documentation

### Docstrings

Use Google-style docstrings:

```python
def method(self, param: str) -> ReturnType:
    """Short description.

    Longer description if needed.

    Args:
        param: Parameter description.

    Returns:
        Return value description.

    Raises:
        ErrorType: When this error occurs.
    """
```

### Update Docs

- Update relevant `.md` files in `docs/`
- Update `README.md` if needed
- Add examples for new features

## Release Process

1. Update version in `pyproject.toml`
2. Update `CHANGELOG.md`
3. Create PR with version bump
4. After merge, create GitHub release
5. CI automatically publishes to PyPI

## Questions?

- Open an issue for bugs or feature requests
- Start a discussion for questions
- Join our community chat

Thank you for contributing! 🎉
