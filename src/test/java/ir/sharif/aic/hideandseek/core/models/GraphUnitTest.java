package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.core.exceptions.AlreadyExistsException;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

class GraphUnitTest {
  private Graph graphInstance;

  @BeforeEach
  void setUp() {
    this.graphInstance = new Graph();
  }

  @Test
  void testAddNode_givenInvalidNodes_throwsValidationException() {
    var invalidNodes = new Node[] {new Node(-10), new Node(0), new Node(-1)};

    for (Node invalidNode : invalidNodes) {
      assertThatThrownBy(() -> graphInstance.addNode(invalidNode))
          .isInstanceOf(ValidationException.class);
    }
  }

  @Test
  void testAddNode_givenDuplicatedNode_throwsAlreadyExistsException() {
    var node = new Node(1);
    graphInstance.addNode(node);

    assertThatThrownBy(() -> graphInstance.addNode(node))
        .isInstanceOf(AlreadyExistsException.class);
  }

  @Test
  void testAddPath_givenInvalidNodesOrPath_throwsValidationException() {
    @AllArgsConstructor
    class TestCase {
      Path path;
      Node first;
      Node second;
    }

    var testCases =
        new TestCase[] {
          new TestCase(new Path(1, 1, 2, -10), new Node(1), new Node(2)),
          new TestCase(new Path(1, 1, 1, 10), new Node(1), new Node(1)),
          new TestCase(new Path(1, -1, 2, 10.0), new Node(-1), new Node(2)),
          new TestCase(new Path(1, 1, -2, 10.0), new Node(1), new Node(-2)),
        };

    for (TestCase t : testCases) {
      assertThatThrownBy(() -> graphInstance.addPath(t.path, t.first, t.second))
          .isInstanceOf(ValidationException.class);
      assertThat(graphInstance.isEmpty()).isTrue();
    }
  }

  @Test
  void testAddPath_givenDuplicatedPath_throwsAlreadyExistsException() {
    @AllArgsConstructor
    class TestCase {
      Path path;
      Node first;
      Node second;
    }

    var cases =
        new TestCase[] {
          new TestCase(new Path(1, 1, 2, -10), new Node(1), new Node(2)),
          new TestCase(new Path(1, 1, 1, 10), new Node(1), new Node(1)),
          new TestCase(new Path(1, -1, 2, 10.0), new Node(-1), new Node(2)),
          new TestCase(new Path(1, 1, -2, 10.0), new Node(1), new Node(-2)),
        };

    for (TestCase t : cases) {
      assertThatThrownBy(() -> graphInstance.addPath(t.path, t.first, t.second))
          .isInstanceOf(ValidationException.class);
      assertThat(graphInstance.isEmpty()).isTrue();
    }
  }

  @Test
  void testGetNodeById_givenExistingNodeId_returnsTheNode() {
    var expectingNode = new Node(1);
    graphInstance.addNode(expectingNode);
    var gotNode = graphInstance.getNodeById(1);

    assertThat(gotNode).isEqualTo(expectingNode);
  }

  @Test
  void testGetPathById_givenExistingPathId_returnsThePath() {
    var firstNode = new Node(1);
    var secondNode = new Node(2);
    var expectedPath = new Path(1, 1, 2, 10);

    graphInstance.addNode(new Node(1));
    graphInstance.addNode(new Node(2));
    graphInstance.addPath(new Path(1, 1, 2, 10), firstNode, secondNode);

    var gotPath = graphInstance.getPathById(1);

    assertThat(gotPath).isEqualTo(expectedPath);
  }

  @Test
  void testToProto_calledOnAPopulatedGraph_returnsMatchingInfo() {
    // populate graph
    List<Node> nodes = new ArrayList<>();
    List<Path> paths = new ArrayList<>();

    for (int i = 1; i <= 5; i++) {
      var newNode = new Node(i);
      graphInstance.addNode(newNode);
      nodes.add(newNode);
      if (i > 1) {
        var firstNode = graphInstance.getNodeById(i - 1);
        var secondNode = graphInstance.getNodeById(i);
        var path = new Path(i - 1, firstNode.getId(), secondNode.getId(), 10);
        paths.add(path);
        graphInstance.addPath(path, firstNode, secondNode);
      }
    }

    var gotProto = graphInstance.toProto();

    assertThat(gotProto.getNodesList())
        .hasSameElementsAs(nodes.stream().map(Node::toProto).toList());

    assertThat(gotProto.getPathsList())
        .hasSameElementsAs(paths.stream().map(Path::toProto).toList());
  }
}
