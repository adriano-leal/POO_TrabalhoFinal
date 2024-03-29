

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Jogo extends Application {
    public static final int CARD_WIDTH = 70;
    public static final int CARD_HEIGHT = 60;

    private ControleDeJogadas cJog;
    private static Map<String, Image> imagens;
    private Carta cartaCorrente, ultimaCartaAberta;
    private Carta primCartaComp,segCartaComp;
    private TextField tfPontosHumano;
   

    public static void main(String[] args) {
        launch(args);
    }

    public static Image getImage(String name) {
        return imagens.get(name);
    }

    private static void loadImagens() {
        imagens = new HashMap<>();

        // Armazena a imagem das costas das cartas
        Image aux = new Image("file:Imagens\\back.jpg");
        imagens.put("back", aux);

        // Armazena a imagem da carta vazia
        imagens.put("empty", null);

        // Armazena as demais imagens
        for (int i = 1; i <= ControleDeJogadas.NUMPARES; i++) {
            aux = new Image("file:Imagens\\img" + i + ".jpg");
            imagens.put("img" + i, aux);
        }
    }

    public void restart(Stage stage) {
        start(stage);
    }
    
    @Override
    public void start(Stage primaryStage) {
        // Carrega imagens
        loadImagens();
        
        // Cria o controle do jogo
        cJog = new ControleDeJogadas();
        ultimaCartaAberta = null;
        cartaCorrente = null;

        // Configura a interface com o usuario
        primaryStage.setTitle("Jogo da memoria");
        GridPane tab = new GridPane();
        tab.setAlignment(Pos.CENTER);
        tab.setHgap(10);
        tab.setVgap(10);
        tab.setPadding(new Insets(25, 25, 25, 25));

        
        // Cria os botoes das cartas
        for (int lin = 0; lin < ControleDeJogadas.NLIN; lin++) {
            for (int col = 0; col < ControleDeJogadas.NCOL; col++) {
                Carta bt = cJog.getCarta(lin, col);
                bt.setPosicao(new Posicao(lin,col));
                bt.setOnAction(e -> viraCarta(e));
                tab.add(bt, col, lin);
            }
        }

        // Linha para exibir os pontos humano
        HBox lin1 = new HBox(10);
        lin1.getChildren().add(new Label("Pontos humano: "));
        tfPontosHumano = new TextField("5");
        tfPontosHumano.setText(""+cJog.getPontosHumano());
        tfPontosHumano.setEditable(false);
        lin1.getChildren().add(tfPontosHumano);

        /* Linha para exibir os pontos computador*/
        HBox lin2 = new HBox(10);
        lin2.getChildren().add(new Label("Pontos computador: "));
        TextField tfPontosComputador = new TextField("5");
        tfPontosComputador.setText(""+cJog.getPontosComputador());
        tfPontosComputador.setEditable(false);
        lin2.getChildren().add(tfPontosComputador);
        
        //monta a cena com uma caixa vertical
        VBox cena = new VBox();
        
        Label label = new Label ("Teste de label");
        cena.getChildren().add(label);

        Button button = new Button ("Reiniciar");
        button.setOnAction(e -> {
            restart(primaryStage);
        });
        cena.getChildren().add(button);

        TextField text = new TextField("Teste de texto");
        cena.getChildren().add(text);

        cena.getChildren().addAll(lin1);
        cena.getChildren().addAll(lin2);
        cena.getChildren().addAll(tab);

        // Monta a cena e exibe
        Scene scene = new Scene(cena);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void viraCarta(ActionEvent e) {
        if (cartaCorrente != null){ // Se esta aguardando análise, retorna
            return;
        }
        // Identifica a carta acionada e reage de acordo
        Carta but = (Carta) e.getSource();
        if (but.getState() == CardState.USADA) {
            return;
        } else if (but.getState() == CardState.ABERTA) {
            return;
        } else {
            // Abre carta
            but.abre();
            but.defineImagem();
            // anota a carta corrente
            if (ultimaCartaAberta == null){
                ultimaCartaAberta = but;
                // informa a primeira carta
                cJog.setCarta(but);
            }else{
                cartaCorrente = but;
                // Programa a analise da jogada em 2 segundo para permitr
                // que o jogador veja as cartas abertas
                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(1500),
                                 ae -> analisaJogada())
                );
                timeline.play();
            }
        }
    }

    public void analisaJogada() {
        GameState state = cJog.setCarta(cartaCorrente);
        switch (state) {
        case ABRIU_CARTA1:
            ultimaCartaAberta = cartaCorrente;
            cartaCorrente.defineImagem();
            return;
        case NAO_ACHOU_PAR:
            ultimaCartaAberta.defineImagem();
            cartaCorrente.defineImagem();
            break;
        case ACHOU_PAR:
            ultimaCartaAberta.defineImagem();
            cartaCorrente.defineImagem();
            break;
        case FIMDEPARTIDA:
            String str = "Humano: "+cJog.getPontosHumano()+" pontos\n";
            str += "Computador: "+cJog.getPontosComputador()+" pontos\n";
            Alert msgBox = new Alert(AlertType.INFORMATION);
            msgBox.setHeaderText("Fim de Jogo");
            msgBox.setContentText(str);
            msgBox.show();
            return;
        }
        // Acoes que são iguais depois de se abrir a segunda carta
        ultimaCartaAberta = null;
        cartaCorrente = null;
        // Inicia jogada do computador
        jogadaComputadorParte1();
    }

    public void jogadaComputadorParte1(){
        primCartaComp = cJog.primeiraCartaComputador();
        segCartaComp = cJog.segundaCartaComputador();
        primCartaComp.defineImagem();
        segCartaComp.defineImagem();
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(1000),
                         ae -> jogadaComputadorParte2())
        );
        timeline.play();
    }

    public void jogadaComputadorParte2(){
        if (cJog.completaJogadaComputador() == GameState.FIMDEPARTIDA){
            String str = "Humano: \nPontos:"+cJog.getPontosHumano()+ "\nPares: ";//getNumParesHumano;
            str = str + "Computador: \nPontos:"+cJog.getPontosComputador()+"\nPares: ";//getNumParesComputador;
            Alert msgBox = new Alert(AlertType.INFORMATION);
            msgBox.setHeaderText("Fim de Jogo");
            msgBox.setContentText(str);
            msgBox.show();
            return;
        }
        primCartaComp.defineImagem();
        segCartaComp.defineImagem();
    }
}
