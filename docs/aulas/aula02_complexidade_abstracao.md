# 📘 Material de Estudo – Aula 01
## Complexidade, Limitações Humanas e Abstração em Software

**PUC-SP | Engenharia de Software – ER e APOO**  
**Professor:** Renato Manzan de Andrade  
**Data:** 17 de março de 2026
---

## 1️⃣ Contexto da Aula

A aula aborda a **limitação natural do ser humano em lidar com múltiplas tarefas e alta complexidade**, fazendo um paralelo direto com os desafios enfrentados no desenvolvimento e manutenção de software.

O ponto de partida é a constatação de que o ser humano **não é verdadeiramente multitarefa**. O que existe, na prática, é uma troca constante de foco entre tarefas, o que consome energia mental e reduz a qualidade do trabalho.

---

## 2️⃣ Limitação Cognitiva Humana

### Capacidade limitada
- O cérebro humano lida com **poucas tarefas simultâneas** (aproximadamente de 3 a 7, e muitas vezes apenas 1 com profundidade).
- A troca constante de contexto gera perda de eficiência.
- Retomar uma tarefa exige esforço mental adicional.

### Analogia utilizada
> *“Fritar o peixe e olhar o gato”*  

Essa metáfora ilustra que tentar fazer muitas coisas ao mesmo tempo aumenta a chance de erro:
- ou o peixe queima
- ou o gato foge
- ou o gato foge com o peixe

---

## 3️⃣ Multitarefa: Um Mito

- Multitarefa real **não existe** para seres humanos.
- O que ocorre é a **suspensão temporária de uma tarefa (thread)** para lidar com outra.
- Cada troca de tarefa gera custo cognitivo.

Pessoas que conseguem “responder enquanto falam ao telefone” apenas alternam rapidamente o foco — ainda assim com perda de eficiência.

---

## 4️⃣ Paralelo com Software

Assim como o ser humano:
- sistemas de software também lidam mal com **excesso de complexidade**;
- quando algo quebra em produção, entender *onde* e *por quê* falhou pode ser difícil;
- logs, métricas e observabilidade ajudam, mas não eliminam a complexidade intrínseca.

Ambientes modernos normalmente incluem:
- múltiplas equipes
- prazos apertados
- integrações
- múltiplos fornecedores
- múltiplas nuvens e sistemas

Tudo isso amplia exponencialmente a complexidade.

---

## 5️⃣ A Necessidade de Lidar com a Complexidade

A Engenharia de Software desenvolveu **técnicas específicas para lidar com essa complexidade**, respeitando as limitações humanas.

A principal técnica introduzida neste contexto é:

### ✅ Abstração

---

## 6️⃣ Abstração na Orientação a Objetos

### O que é abstração?
Abstração é a capacidade de:
- focar apenas no que é **relevante**;
- ignorar detalhes desnecessários em um determinado contexto;
- reduzir a carga cognitiva.

### Exemplo citado
Em um sistema médico de suporte à vida:
- modelam-se apenas **atributos e comportamentos essenciais**;
- descarta-se tudo o que não contribui diretamente para aquele objetivo.

Assim, a abstração atua como um **filtro de complexidade**.

---

## 7️⃣ Por que a Abstração é Essencial?

- O ser humano tem capacidade limitada de processamento mental;
- Sistemas modernos são altamente complexos;
- Sem abstração, o entendimento e a manutenção se tornam inviáveis;
- Abstração permite evoluir sistemas grandes sem perder controle.

---

## 8️⃣ Mensagens-Chave da Aula

### ✅ Desenvolver senso crítico
- Relatórios (como *Chaos Reports*) não devem ser consumidos de forma acrítica.
- É necessário avaliar:
  - quando se aplicam
  - em que contexto
  - o que faz sentido para a sua realidade

### ✅ Entender dimensões de um sistema
- Sistemas possuem dimensão **estática** e **dinâmica**;
- A engenharia de software fornece mecanismos para lidar com ambas.

### ✅ Gestão da complexidade
- Complexidade é inevitável;
- O objetivo não é eliminá-la, mas **organizá-la** e **controlá-la**.

---

## 🧠 Mapa Mental (Texto)

```
Ser Humano
│
├── Capacidade Limitada
│   ├── Poucas tarefas simultâneas
│   ├── Alto custo de troca de contexto
│   └── Perda de foco
│
├── Multitarefa
│   ├── Não existe de fato
│   └── Apenas alternância de foco
│
├── Complexidade
│   ├── Equipes
│   ├── Prazos
│   ├── Integrações
│   └── Sistemas distribuídos
│
├── Software
│   ├── Falhas em produção
│   ├── Debug difícil
│   └── Alta intrincação
│
└── Abstração
    ├── Filtra o que importa
    ├── Reduz carga cognitiva
    ├── Base do OO
    └── Ferramenta contra complexidade
```

---

## Conclusão

A abstração não é apenas um conceito teórico da orientação a objetos, mas uma **resposta prática às limitações humanas** diante da complexidade crescente dos sistemas de software.
