import React from "react";
import { Container, Row, Col } from "react-bootstrap";

const FormContainer = ({ children }) => {
  return (
    <Container>
      <Row className="justify-content-md-center">
        {/* small screen takes 100% and medium screen takes half of it */}
        <Col xs={12} md={6}>
          {/* Using children so that it can be used by other components as well */}
          {children}
        </Col>
      </Row>
    </Container>
  );
};

export default FormContainer;
