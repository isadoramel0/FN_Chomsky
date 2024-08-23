import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class fnc {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Use: java fnc <entrada.txt> <saida.txt>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];
        
        // O mapa associa cada não-terminal (a chave) a um conjunto de produções (valor)
        // Chave S: Valor: "aS | bS"

        Map<String, StringBuilder> producoes = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String linha;
            String naoTermAtt = null;
            
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                
                // Ignorar linhas vazias
                if (linha.isEmpty()) {
                    continue;
                }

                // Se a linha contém o operador de definição, é uma nova regra
                if (linha.contains("->")) {
                    int arrowIndex = linha.indexOf("->");
                    naoTermAtt = linha.substring(0, arrowIndex).trim(); //Extrair o não-terminal
                    String producoesLinha = linha.substring(arrowIndex + 2).trim(); // Extrair as produções
                    
                    // Adicionar a produção ao mapa, separando por |
                    String[] producoesSeparadas = producoesLinha.split("\\|");
                    StringBuilder sb = producoes.getOrDefault(naoTermAtt, new StringBuilder());
                    
                    for (String prod : producoesSeparadas) {
                        String trimmedProd = prod.trim();
                        if (trimmedProd.equals(".")) {
                            trimmedProd = "."; // Substituir ponto por epsilon
                        }
                        if (sb.length() > 0) {
                            sb.append(" | ");
                        }
                        sb.append(trimmedProd);
                    }
                    producoes.put(naoTermAtt, sb);
                } else if (naoTermAtt != null) {
                    // Continuar adicionando regras para o mesmo não-terminal
                    StringBuilder sb = producoes.getOrDefault(naoTermAtt, new StringBuilder());
                    String trimmedProd = linha.trim();
                    if (trimmedProd.equals(".")) {
                        trimmedProd = "ε"; // Substituir ponto por epsilon
                    }
                    if (sb.length() > 0) {
                        sb.append(" | ");
                    }
                    sb.append(trimmedProd);
                    producoes.put(naoTermAtt, sb);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Exibir produções para verificação
        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            String naoTerminal = entry.getKey();
            String producao = entry.getValue().toString();
            System.out.println(naoTerminal + " -> " + producao);
        }
    }
}

    


