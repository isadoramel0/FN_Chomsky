import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class fnc {
    public static void recSimbInicial(Map<String, StringBuilder> producoes, String simbInicial) {
        // Dividir as produções do símbolo inicial em produções individuais
        StringBuilder regrasOriginais = producoes.get(simbInicial);
        String[] producoesArray = regrasOriginais.toString().split("\\|");

        boolean temRecursao = false;
            for (String regra : producoesArray) {
                regra = regra.trim();
                if (regra.contains(simbInicial)) {
                    temRecursao = true;
                    break;
                }
            }
            if (!temRecursao) {
                return;
            }

        // Criar um novo símbolo não-terminal para lidar com a recursão
        String novoSimbolo = simbInicial + "'";
        StringBuilder novasProducoes = new StringBuilder();
        StringBuilder producoesNaoRecursivas = new StringBuilder();

        // Processar as produções
        for (String regra : producoesArray) {
            regra = regra.trim();
            if (regra.contains(simbInicial)) {
                // Se a regra contém o símbolo inicial em qualquer parte
                // Mover a parte recursiva para o novo símbolo
                String regraSemRecursao = regra.replaceAll(simbInicial, novoSimbolo).trim();
                if (novasProducoes.length() > 0) {
                    novasProducoes.append(" | ");
                }
                novasProducoes.append(regraSemRecursao);
            } else {
                // Adicionar as produções não recursivas ao novo símbolo
                if (producoesNaoRecursivas.length() > 0) {
                    producoesNaoRecursivas.append(" | ");
                }
                producoesNaoRecursivas.append(regra);
            }
        }

        // Atualizar a regra do símbolo inicial para apontar somente para o novo símbolo
        producoes.put(simbInicial, new StringBuilder(novoSimbolo));

        // Adicionar as regras não recursivas ao novo símbolo
        if (producoesNaoRecursivas.length() > 0) {
            if (novasProducoes.length() > 0) {
                novasProducoes.append(" | ");
            }
            novasProducoes.append(producoesNaoRecursivas);
        }
        // Adicionar o novo símbolo ao mapa de produções
        producoes.put(novoSimbolo, novasProducoes);
    }

    // Método para ler a gramática de glc1.txt
    private static Map<String, StringBuilder> leituraArq(String inputFile) throws IOException {
        // Usamos LinkedHashMap para garantir a ordem de inserção
        Map<String, StringBuilder> producoes = new LinkedHashMap<>();
        try (BufferedReader leitura = new BufferedReader(new FileReader(inputFile))) {
            String linha;
            String simbolo = null;
            while ((linha = leitura.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) {
                    continue;
                }

                if (linha.contains("->")) {
                    int separacao = linha.indexOf("->");
                    simbolo = linha.substring(0, separacao).trim();
                    String producoesLinha = linha.substring(separacao + 2).trim();
                    String[] producoesSep = producoesLinha.split("\\|");
                    StringBuilder sb = producoes.getOrDefault(simbolo, new StringBuilder());
                    for (String prod : producoesSep) {
                        String trimmedProd = prod.trim();
                        if (sb.length() > 0) {
                            sb.append(" | ");
                        }
                        sb.append(trimmedProd); 
                    }
                    producoes.put(simbolo, sb);
                }
            }
        }
        return producoes;
    }

    // Método para escrever em glc1_fnc.txt
    private static void escritaArq(Map<String, StringBuilder> producoes, String outputFile, String simbInicial) throws IOException {
        try (BufferedWriter escrita = new BufferedWriter(new FileWriter(outputFile))) {
            // Primeiro escrever o símbolo inicial e o seu novo símbolo imediatamente após
            escrita.write(simbInicial + " -> " + producoes.get(simbInicial).toString());
            escrita.newLine();

            String novoSimbolo = simbInicial + "'";
            if (producoes.containsKey(novoSimbolo)) {
                escrita.write(novoSimbolo + " -> " + producoes.get(novoSimbolo).toString());
                escrita.newLine();
            }

            // Escrever os demais símbolos, exceto o símbolo inicial e seu novo símbolo
            for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
                if (!entry.getKey().equals(simbInicial) && !entry.getKey().equals(novoSimbolo)) {
                    escrita.write(entry.getKey() + " -> " + entry.getValue().toString());
                    escrita.newLine();
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Use: java fnc.java <glc1.txt> <glc1_fnc.txt>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            // Ler a gramática do arquivo de entrada
            Map<String, StringBuilder> producoes = leituraArq(inputFile);

            // Identificar o símbolo inicial (primeira chave lida no mapa)
            String simbInicial = producoes.keySet().iterator().next();

            // Remover a recursão do símbolo inicial se houver
            recSimbInicial(producoes, simbInicial);

            // Escrever a gramática transformada no arquivo de saída
            escritaArq(producoes, outputFile, simbInicial);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

