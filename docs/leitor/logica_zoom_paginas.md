# Lógica de Zoom Relativo a Páginas — Reader Online

## Visão Geral

O sistema de leitura online exibe um livro de **71 páginas** com dois indicadores simultâneos de progresso:

- **Page X of 71** → número da página lógica atual visível na tela
- **N%** → percentual de progresso absoluto no livro inteiro
- **Zoom** → nível de ampliação da exibição do conteúdo (50% ou 100%)

---

## Tabela de Observações

| Imagem | Zoom | Page | Total Pages | % Progresso | Observação |
|--------|------|------|-------------|-------------|------------|
| img1   | 50%  | 21   | 71          | 26%         | Capítulo 6 – Nouns (início) |
| img2   | 50%  | 26   | 71          | 34%         | Fim do capítulo Nouns (exercises) |
| img3   | 100% | 21   | 71          | 26%         | Mesmo ponto que img1, zoom dobrado |
| img4   | 100% | 22   | 71          | 28%         | Avanço de 1 página lógica = +2% |
| img5   | 100% | 24   | 71          | 30%         | Avanço de 2 páginas lógicas = +2% |
| img6   | 100% | 25   | 71          | 34%         | Avanço de 1 página lógica = +4% |
| img7   | 100% | 27   | 71          | 35%         | Avanço de 2 páginas lógicas = +1% |

---

## Lógica Identificada

### 1. O Zoom NÃO afeta a posição/progresso

- `img1` (zoom 50%, page 21, 26%) e `img3` (zoom 100%, page 21, 26%) são **idênticos em posição**
- O zoom altera apenas **quantas "páginas lógicas" cabem na tela** de uma vez
- O progresso percentual e o número de página são independentes do zoom

### 2. Relação entre Page e Percentual

```
% progresso ≈ (página_atual / total_páginas) × 100
```

Verificação:
- 21 / 71 = 29,5% → sistema mostra 26% → há um **offset/deslocamento**
- 26 / 71 = 36,6% → sistema mostra 34%
- 27 / 71 = 38,0% → sistema mostra 35%

**Conclusão:** O percentual exibido **não é calculado diretamente pela página atual dividida pelo total**. Há um deslocamento fixo de aproximadamente **3 a 4 pontos percentuais para baixo**, o que indica que o cálculo provavelmente usa:

```
% real = (posição_caractere_atual / total_caracteres_livro) × 100
```

Ou seja, o `%` é baseado em **posição de caracteres/palavras no texto**, não em número de páginas. As páginas são renderizadas dinamicamente conforme o zoom, mas o progresso absoluto é medido no conteúdo do texto.

### 3. Comportamento do Zoom sobre Páginas Lógicas

- **Zoom 50%:** mostra mais conteúdo por tela → avança menos "páginas lógicas" por scroll
  - De page 21 a page 26 = 5 páginas lógicas para ir de 26% → 34% (+8%)
- **Zoom 100%:** mostra menos conteúdo por tela → cada scroll avança menos %
  - De page 21 a page 27 = 6 páginas lógicas para ir de 26% → 35% (+9%)

A diferença é pequena porque as imagens zoom 50% cobrem aproximadamente o **dobro do conteúdo visual** por "página" exibida, mas o conteúdo total percorrido é semelhante.

### 4. Cálculo Estimado de Densidade de Conteúdo por Zoom

```
Zoom 50%:  1 "tela" ≈ ~2 páginas lógicas de conteúdo
Zoom 100%: 1 "tela" ≈ ~1 página lógica de conteúdo
```

Portanto: **zoom 50% renderiza aproximadamente 2x mais conteúdo por "página" que o zoom 100%**

### 5. Estrutura dos Indicadores Visuais

```
[barra de progresso horizontal] — posição da bolinha corresponde ao %
Bottom-left:  "X mins left in chapter"
Bottom-center: "Page X of 71 • N%"
Bottom-right:  "X mins left in book"
```

---

## Regras para o Agente de Verificação

O agente deve verificar as seguintes invariantes:

### Regra 1 — Mesma posição = mesmo % independente do zoom
```
IF zoom_muda AND posição_texto_não_muda
THEN page_number NÃO muda E percentual NÃO muda
```

### Regra 2 — Percentual baseado em posição de texto (não de página)
```
% exibido ≠ (página_atual / total_páginas) × 100
% exibido ≈ (offset_texto_atual / total_texto) × 100
```
O agente deve usar a posição absoluta no texto como fonte de verdade para o `%`.

### Regra 3 — Páginas lógicas crescem com o avanço
```
IF page aumenta THEN % deve aumentar ou manter (nunca diminuir)
IF % aumenta THEN page deve aumentar ou manter (nunca diminuir)
```

### Regra 4 — Zoom 50% ≈ 2× mais conteúdo por tela
```
conteudo_por_tela(zoom=50%) ≈ 2 × conteudo_por_tela(zoom=100%)
delta_paginas_por_tela(zoom=50%) ≈ 2 × delta_paginas_por_tela(zoom=100%)
```

### Regra 5 — Barra de progresso coerente com %
```
posicao_barra ≈ (N% / 100) × largura_total_barra
```

### Regra 6 — Consistência de "mins left"
```
mins_left_in_book deve diminuir conforme % aumenta
mins_left_in_chapter deve diminuir conforme avança no capítulo
```

---

## Anomalias a Detectar

| Anomalia | Sintoma |
|----------|---------|
| Zoom alterou progresso | Mesmo texto, zoom diferente → % ou page diferente |
| Progresso regressivo | % ou page diminuiu sem ação do usuário |
| Incoerência barra vs % | Bolinha não corresponde ao valor numérico |
| Page out of bounds | Page < 1 ou Page > total_pages |
| % fora de range | % < 0 ou % > 100 |
| Salto anômalo | Delta de % muito grande entre páginas consecutivas |

---

## Referência das Imagens

| Arquivo | Zoom | Page | % |
|---------|------|------|---|
| img1_zoom50_page21_26pct.png  | 50%  | 21 | 26% |
| img2_zoom50_page26_34pct.png  | 50%  | 26 | 34% |
| img3_zoom100_page21_26pct.png | 100% | 21 | 26% |
| img4_zoom100_page22_28pct.png | 100% | 22 | 28% |
| img5_zoom100_page24_30pct.png | 100% | 24 | 30% |
| img6_zoom100_page25_34pct.png | 100% | 25 | 34% |
| img7_zoom100_page27_35pct.png | 100% | 27 | 35% |
