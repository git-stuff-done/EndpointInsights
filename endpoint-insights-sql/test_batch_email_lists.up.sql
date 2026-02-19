CREATE TABLE test_batch_email_lists (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id uuid NOT NULL,
    email VARCHAR(320) NOT NULL, -- Max email length, 320 characters

    CONSTRAINT fk_batch FOREIGN KEY (batch_id) REFERENCES test_batch(id) ON DELETE CASCADE
);

CREATE INDEX idx_test_batch_email_lists_batch_id ON test_batch_email_lists(batch_id);